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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> times = new ArrayList<TimeRange>();
    HashSet<String> attendees = new HashSet<String> ();
    for (String attendee : request.getAttendees()) {
      attendees.add(attendee);
    }
    int[] possible = new int[48];
    for (Event event : events) {
      int countAttendees = 0;
      Set<String> eventAttendees = event.getAttendees();
      for (String attendee : eventAttendees) {
        if (attendees.contains(attendee)) {
          countAttendees += 1;
        }
      }
      int startMinute = event.getWhen().start();
      for (int i = 0; i < event.getWhen().duration() / 30; i++) {
        possible[startMinute / 30 + i] += countAttendees;
      }
    }
    int start = -1;
    int duration = 0;
    for (int i = 0; i < 48; i++) {
      if (possible[i] == 0) {
        if (start == -1) {
          start = i;
        }
        duration += 1;
      } else {
        if (duration >= request.getDuration() / 30) {
          TimeRange test = TimeRange.fromStartDuration(start * 30, duration * 30);
          times.add(test);
        }
        start = -1;
        duration = 0;
      }
    }
    if (duration > request.getDuration() / 30) {
      TimeRange test = TimeRange.fromStartDuration(start * 30, duration * 30);
      times.add(test);
    }
    Collections.sort(times, TimeRange.ORDER_BY_START);
    return times;
  }
}
