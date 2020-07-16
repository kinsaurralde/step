// Copyright 2019 Google LLC
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> possibleTimes = new ArrayList<TimeRange>();
    ArrayList<TimeRange> optionalTimes = new ArrayList<TimeRange>();
    ArrayList<String> attendees = new ArrayList<String>(request.getAttendees());
    ArrayList<String> optionalAttendees = new ArrayList<String>(request.getOptionalAttendees());
    possibleTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true));
    if (attendees.size() == 0 && optionalAttendees.size() == 0) {
      return possibleTimes;
    }
    
    long duration = request.getDuration();
    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return new ArrayList<TimeRange>();
    }
    boolean hasRequired = false;
    for (Event event : events) {
      boolean validEvent = false;
      boolean optionalOnly = false;
      for (String attendee : event.getAttendees()) {
        if (attendees.contains(attendee)) {
          validEvent = true;
          hasRequired = true;
          break;
        }
      }
      if (!validEvent) {
        for (String attendee : event.getAttendees()) {
          if (optionalAttendees.contains(attendee)) {
            validEvent = true;
            optionalOnly = true;
            break;
          }
        }
      }
      if (!validEvent) {
        continue;
      }
      TimeRange eventTime = event.getWhen();
      Queue<TimeRange> timeCheck = new LinkedList<TimeRange>(possibleTimes);
      while (timeCheck.peek() != null) {
        TimeRange timeRange = timeCheck.remove();
        if (timeRange.overlaps(eventTime)) {
          if (optionalOnly) {
            optionalTimes.add(eventTime);
          } else {
            addTime(possibleTimes, optionalTimes, timeRange, eventTime, duration, optionalOnly);
          }
        }
      }
    }
    for (TimeRange optionalTime : optionalTimes) {
      if (sumAvailibleTime(possibleTimes) < optionalTime.duration() + duration && hasRequired) {
        continue;
      }
      for (ListIterator<TimeRange> iter = possibleTimes.listIterator(); iter.hasNext();) {
        TimeRange timeRange = iter.next();
        int timeRangeStart = timeRange.start();
        int timeRangeEnd = timeRange.end();
        int eventTimeStart = optionalTime.start();
        int eventTimeEnd = optionalTime.end();
        if (timeRange.contains(optionalTime)) {
          iter.remove();
          if (isTimeSlotValid(timeRangeStart, eventTimeStart, duration)) {
            iter.add(TimeRange.fromStartEnd(timeRangeStart, eventTimeStart, false));
          }
          if (isTimeSlotValid(eventTimeEnd, timeRangeEnd, duration)) {
            iter.add(TimeRange.fromStartEnd(eventTimeEnd, timeRangeEnd, false));
          }
        } else if (timeRange.overlaps(optionalTime)) {
          if (timeRangeStart < eventTimeStart && timeRangeEnd < eventTimeEnd) {
            iter.remove();
            iter.add(TimeRange.fromStartEnd(timeRangeStart, eventTimeStart, false));
          } else if (timeRangeStart > eventTimeStart && timeRangeEnd > eventTimeEnd) {
            iter.remove();
            iter.add(TimeRange.fromStartEnd(eventTimeEnd, timeRangeEnd, false));
          }
        }
      }
    }
    return possibleTimes;
  }

  private int sumAvailibleTime(ArrayList<TimeRange> times) {
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

  private void addTime(ArrayList<TimeRange> possibleTimes, ArrayList<TimeRange> optionalTimes, TimeRange timeRange, TimeRange eventTime, long duration, boolean optionalOnly) {
    int timeRangeStart = timeRange.start();
    int timeRangeEnd = timeRange.end();
    int eventTimeStart = eventTime.start();
    int eventTimeEnd = eventTime.end();
    if (timeRange.equals(eventTime)) {
      if (optionalOnly) {
        optionalTimes.add(TimeRange.fromStartEnd(timeRangeStart, timeRangeEnd, false));
      } else {
        possibleTimes.add(TimeRange.fromStartEnd(timeRangeStart, timeRangeEnd, false));
      }
    } else if (timeRange.contains(eventTime)) { // possible time contains event time
      if (isTimeSlotValid(timeRangeStart, eventTimeStart, duration)) {
        if (optionalOnly) {
          optionalTimes.add(TimeRange.fromStartEnd(timeRangeStart, eventTimeStart, false));
        } else {
          possibleTimes.add(TimeRange.fromStartEnd(timeRangeStart, eventTimeStart, false));
        }
      }
      if (isTimeSlotValid(eventTimeEnd, timeRangeEnd, duration)) {
        if (optionalOnly) {
          optionalTimes.add(TimeRange.fromStartEnd(eventTimeEnd, timeRangeEnd, false));
        } else {
          possibleTimes.add(TimeRange.fromStartEnd(eventTimeEnd, timeRangeEnd, false));
        }
      }
    } else if (timeRangeStart < eventTimeStart && timeRangeEnd < eventTimeEnd) {
      if (isTimeSlotValid(timeRangeStart, eventTimeStart, duration)) {
        if (optionalOnly) {
          optionalTimes.add(TimeRange.fromStartEnd(timeRangeStart, eventTimeStart, false));
        } else {
          possibleTimes.add(TimeRange.fromStartEnd(timeRangeStart, eventTimeStart, false));
        }
      }
    } else if (timeRangeStart > eventTimeStart && timeRangeEnd > eventTimeEnd) {
      if (isTimeSlotValid(eventTimeEnd, timeRangeEnd, duration)) {
        if (optionalOnly) {
          optionalTimes.add(TimeRange.fromStartEnd(eventTimeEnd, timeRangeEnd, false));
        } else {
          possibleTimes.add(TimeRange.fromStartEnd(eventTimeEnd, timeRangeEnd, false));
        }
      }
    }
    if (!optionalOnly) {
      possibleTimes.remove(timeRange);
    }
  }
}
