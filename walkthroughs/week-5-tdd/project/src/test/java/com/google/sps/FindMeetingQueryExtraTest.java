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
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class FindMeetingQueryExtraTest {
  private static final Collection<Event> NO_EVENTS = Collections.emptySet();
  private static final Collection<String> NO_ATTENDEES = Collections.emptySet();

  // Some people that we can use in our tests.
  private static final String PERSON_A = "Person A";
  private static final String PERSON_B = "Person B";
  private static final String PERSON_C = "Person C";
  private static final String PERSON_D = "Person D";
  private static final String PERSON_E = "Person E";
  private static final String PERSON_F = "Person F";

  // All dates are the first day of the year 2020.
  private static final int TIME_0800AM = TimeRange.getTimeInMinutes(8, 00);
  private static final int TIME_0830AM = TimeRange.getTimeInMinutes(8, 30);
  private static final int TIME_0845AM = TimeRange.getTimeInMinutes(8, 45);
  private static final int TIME_0900AM = TimeRange.getTimeInMinutes(9, 00);
  private static final int TIME_0930AM = TimeRange.getTimeInMinutes(9, 30);
  private static final int TIME_1000AM = TimeRange.getTimeInMinutes(10, 00);
  private static final int TIME_1015AM = TimeRange.getTimeInMinutes(10, 15);
  private static final int TIME_1030AM = TimeRange.getTimeInMinutes(10, 30);
  private static final int TIME_1100AM = TimeRange.getTimeInMinutes(11, 00);
  private static final int TIME_1130AM = TimeRange.getTimeInMinutes(11, 30);
  private static final int TIME_1200PM = TimeRange.getTimeInMinutes(12, 00);
  private static final int TIME_0330PM = TimeRange.getTimeInMinutes(15, 30);
  private static final int TIME_0600PM = TimeRange.getTimeInMinutes(18, 00);

  private static final int DURATION_15_MINUTES = 15;
  private static final int DURATION_30_MINUTES = 30;
  private static final int DURATION_60_MINUTES = 60;
  private static final int DURATION_90_MINUTES = 90;
  private static final int DURATION_1_HOUR = 60;
  private static final int DURATION_2_HOUR = 120;

  private FindMeetingQuery query;

  @Before
  public void setUp() {
    query = new FindMeetingQuery();
  }

  @Test
  public void singleAttendeeOverlappingEvents() {
    // Have an overlapping event for a person. We should only see two options.
    //
    // Events  :       |--A--|
    //                     |--A--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_60_MINUTES),
            Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void secondAttendeeWithoutEvents() {
    // Attendee without event should not affect options.
    //
    // Events  :       |--A--|
    //                     |--A--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_60_MINUTES),
            Arrays.asList(PERSON_A)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyAttendeeIsConsideredWithMoreThanTwoAttendees() {
    // Have each person have different events. We should see two options because each person has
    // split the restricted times.
    //
    // Events  :       |--A--||--C--||--B--|
    // Day     : |--------------------------------|
    // Options : |--1--|                    |--3--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_C)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B, PERSON_C), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyAttendeeIsConsideredWithBookedOptional() {
    // Optional attendee can not fit
    //
    // Events  :       |--A--|     |--B--|
    // Optional: |--------------C---------------|
    // Day     : |------------------------------|
    // Options : |--1--|                  |--2--|
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)),
        new Event("Event 3",
            TimeRange.fromStartDuration(TimeRange.START_OF_DAY, TimeRange.WHOLE_DAY.duration()),
            Arrays.asList(PERSON_C)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyAttendeeIsConsideredWithMiddleEventOptional() {
    // Optional attendee can fit
    //
    // Events  :       |--A--|     |--B--|
    // Optional:             |--C--|
    // Day     : |------------------------------|
    // Options : |--1--|                 |--2--|
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_C)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoomWithBookedOptional() {
    // Have one required and one optional, but make it so that there is just enough room at one
    // point in the day to have the meeting without optional attendee
    //
    // Events  : |---A---|        |---A---|
    // Optional:         |-B-|
    // Day     : |---------------------|
    // Options :         |----1---|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
            Arrays.asList(PERSON_A)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_15_MINUTES),
            Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void zeroMandatoryTwoOptionalWithEvents() {
    // All optional with time slots open
    //
    // Events  :
    // Optional:       |--A--|     |--B--|
    // Day     : |------------------------------|
    // Options : |--1--|     |--2--|      |--3--|
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void zeroMandatoryTwoBookedOptionalWholeDay() {
    // Only optional attendees with full day events
    //
    // Events  :
    // Optional: |-----------------A-B------------------|
    // Day     : |--------------------------------------|
    // Options : |--------------------------------------|
    Collection<Event> events = Arrays.asList(
        new Event("Event 1",
            TimeRange.fromStartDuration(TimeRange.START_OF_DAY, TimeRange.WHOLE_DAY.duration()),
            Arrays.asList(PERSON_A)),
        new Event("Event 2",
            TimeRange.fromStartDuration(TimeRange.START_OF_DAY, TimeRange.WHOLE_DAY.duration()),
            Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_1_HOUR);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void MoreThanTwoAttendeesAndOneOptionalAttendee() {
    // Have each person have different events. We should see two options because each person has
    // split the restricted times.
    //
    // Events  :       |--A--||--C--||--B--|
    // Optional:                           |--D--|
    // Day     : |--------------------------------------|
    // Options : |--1--|                          |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_C)),
        new Event("Event 4", TimeRange.fromStartDuration(TIME_0930AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_D)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B, PERSON_C), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_D);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void MoreThanTwoAttendeesAndMoreThanTwoOptionalAttendees() {
    // Have each person have different events. We should see two options because each person has
    // split the restricted times.
    //
    // Events  :       |--A--||--C--||--B--|
    // Optional:                            |--D--||--E--||--F--|
    // Day     : |----------------------------------------------------|
    // Options : |--1--|                                        |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_C)),
        new Event("Event 4", TimeRange.fromStartDuration(TIME_0930AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_D)),
        new Event("Event 5", TimeRange.fromStartDuration(TIME_1000AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_E)),
        new Event("Event 6", TimeRange.fromStartDuration(TIME_1030AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_F)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B, PERSON_C), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_D);
    request.addOptionalAttendee(PERSON_E);
    request.addOptionalAttendee(PERSON_F);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_1100AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void MoreThanTwoAttendeesAndMoreThanTwoUnavailableOptionalAttendees() {
    // Optional attendees have whole day event so cant come to meeting
    //
    // Events  :       |--A--||--C--||--B--|
    // Optional: |-------------D-E-F--------------|
    // Day     : |--------------------------------|
    // Options : |--1--|                    |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_C)),
        new Event("Event 5",
            TimeRange.fromStartDuration(TimeRange.START_OF_DAY, TimeRange.WHOLE_DAY.duration()),
            Arrays.asList(PERSON_D, PERSON_E, PERSON_F)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B, PERSON_C), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_D);
    request.addOptionalAttendee(PERSON_E);
    request.addOptionalAttendee(PERSON_F);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }
}
