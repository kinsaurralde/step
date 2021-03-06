// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> possibleTimes = new ArrayList<TimeRange>();
    ArrayList<TimeRange> optionalTimes = new ArrayList<TimeRange>();
    ArrayList<String> attendees = new ArrayList<String>(request.getAttendees());
    ArrayList<String> optionalAttendees = new ArrayList<String>(request.getOptionalAttendees());
    long duration = request.getDuration();

    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return possibleTimes;
    }
    possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true));
    if (attendees.size() == 0 && optionalAttendees.size() == 0) {
      return possibleTimes;
    }

    boolean hasRequired = false;
    for (Event event : events) {
      TimeRange eventTime = event.getWhen();
      boolean hasRequiredAttendees = false;
      boolean hasOptionalAttendees = false;
      for (String attendee : event.getAttendees()) {
        if (attendees.contains(attendee)) {
          hasRequiredAttendees = true;
          hasRequired = true;
        }
        if (optionalAttendees.contains(attendee)) {
          hasOptionalAttendees = true;
        }
      }
      if (!hasRequiredAttendees && !hasOptionalAttendees) {
        continue;
      }
      if (!hasRequiredAttendees && hasOptionalAttendees) {
        optionalTimes.add(eventTime);
        continue;
      }
      addTime(possibleTimes, eventTime, duration);
    }

    for (TimeRange optionalTime : optionalTimes) {
      if (sumAvailableTime(possibleTimes) < optionalTime.duration() + duration && hasRequired) {
        continue; // no space for optional event since required meeting takes priority
      }
      addTime(possibleTimes, optionalTime, duration);
    }
    return possibleTimes;
  }

  /** 
   *  Check if event overlaps or is contained by a possible meeting time
   *  If it is, split possible meeting time to remove the time range of the event
  */
  private void addTime(ArrayList<TimeRange> times, TimeRange eventTime, long duration) {
    for (ListIterator<TimeRange> iter = times.listIterator(); iter.hasNext();) {
      TimeRange timeRange = iter.next();
      int timeRangeStart = timeRange.start();
      int timeRangeEnd = timeRange.end();
      int eventTimeStart = eventTime.start();
      int eventTimeEnd = eventTime.end();
      if (timeRange.contains(eventTime)) {
        /* |---- meeting ----| becomes |-- meeting --||-- event --||-- meeting --|
        *    |-- event --|
        */ 
        iter.remove();
        if (isTimeSlotValid(timeRangeStart, eventTimeStart, duration)) {
          iter.add(TimeRange.fromStartEnd(timeRangeStart, eventTimeStart, false));
        }
        if (isTimeSlotValid(eventTimeEnd, timeRangeEnd, duration)) {
          iter.add(TimeRange.fromStartEnd(eventTimeEnd, timeRangeEnd, false));
        }
      } else if (timeRange.overlaps(eventTime)) {
        /* |-- meeting --|       becomes |-- meeting --||-- event --|
        *        |-- event --|
        */
        iter.remove();
        if (timeRangeStart < eventTimeStart && timeRangeEnd < eventTimeEnd) { 
          if (isTimeSlotValid(timeRangeStart, eventTimeStart, duration)) {
            iter.add(TimeRange.fromStartEnd(timeRangeStart, eventTimeStart, false));
          }
        } else if (timeRangeStart > eventTimeStart && timeRangeEnd > eventTimeEnd) {
          if (isTimeSlotValid(eventTimeEnd, timeRangeEnd, duration)) {
            iter.add(TimeRange.fromStartEnd(eventTimeEnd, timeRangeEnd, false));
          }
        }
      }
    }
  }

  private int sumAvailableTime(ArrayList<TimeRange> times) {
    int sum = 0;
    for (TimeRange timeRange : times) {
      sum += timeRange.duration();
    }
    return sum;
  }

  private boolean isTimeSlotValid(int start, int end, long duration) {
    if (start == end) {
      return false;
    }
    return (end - start) >= duration;
  }
}
