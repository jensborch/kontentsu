/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.kontentsu.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Time interval a given item should be present on the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Embeddable
@ValidInterval
public class Interval implements Serializable {

    /**
     * Max date use when "valid to" has not been set. LocalDateTime.MAX can't be used as it exceeded the maximum value of SQL Timestamp.
     */
    public static final ZonedDateTime INFINIT = ZonedDateTime.of(LocalDateTime.of(2099, Month.DECEMBER, 31, 0, 0), ZoneOffset.UTC);

    private static final long serialVersionUID = -239839694581153026L;

    @Column(name = "valid_from")
    @NotNull
    private ZonedDateTime from;

    @Column(name = "valid_to")
    private ZonedDateTime to;

    public Interval(final ZonedDateTime from, final ZonedDateTime to) {
        this.from = from;
        this.to = (to == null) ? INFINIT : to;
    }

    public Interval(final ZonedDateTime from) {
        this(from, null);
    }

    public Interval() {
        this(ZonedDateTime.now(), null);
    }

    public ZonedDateTime getFrom() {
        return from;
    }

    public ZonedDateTime getTo() {
        return to;
    }

    @JsonIgnore
    public boolean isInfinit() {
        return to.toInstant().equals(INFINIT.toInstant());
    }

    /**
     * Returns true intervals share some part of the time-line.
     *
     * @param other the other interval to use for check
     * @return true if intervals overlaps
     */
    public boolean overlaps(final Interval other) {
        if (other == null) {
            return false;
        } else {
            return other.equals(this)
                    || from.compareTo(other.to) < 0 && other.from.compareTo(to) < 0;
        }
    }

    /**
     * Creates a new interval that is the intersection of this interval and the other interval.
     *
     * @param other the other interval to create intersection from
     * @return new intersection interval
     */
    public Optional<Interval> intersection(final Interval other) {
        if (overlaps(other)) {
            ZonedDateTime newFrom = getFrom().isAfter(other.getFrom()) ? getFrom() : other.getFrom();
            ZonedDateTime newTo = getTo().isAfter(other.getTo()) ? other.getTo() : getTo();
            return Optional.of(new Interval(newFrom, newTo));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Given a new interval <code>other</code> this method produces list of all intervals that does not overlap.
     *
     * @param other interval to use
     * @return a list of intervals
     */
    public Set<Interval> disjunctiveUnion(final Interval other) {
        Set<Interval> results = new HashSet<>();
        Optional<Interval> intersection = intersection(other);
        if (intersection.isPresent()) {
            ZonedDateTime min = min(other);
            if (min.isBefore(intersection.get().getFrom())) {
                results.add(new Interval(min, intersection.get().getFrom()));
            }
            ZonedDateTime max = max(other);
            if (max.isAfter(intersection.get().getTo())) {
                results.add(new Interval(intersection.get().getTo(), max));
            }
        } else {
            results.add(this);
        }
        return results;
    }

    private List<ZonedDateTime> times() {
        List<ZonedDateTime> result = new ArrayList<>(2);
        result.add(from);
        result.add(to);
        return result;
    }

    public ZonedDateTime min(final Interval other) {
        return Stream.concat(times().stream(), other.times().stream()).min((t1, t2) -> t1.compareTo(t2)).get();
    }

    public ZonedDateTime max(final Interval other) {
        return Stream.concat(times().stream(), other.times().stream()).max((t1, t2) -> t1.compareTo(t2)).get();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.from);
        hash = 59 * hash + Objects.hashCode(this.to);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Interval other = (Interval) obj;
        return from.toInstant().equals(other.from.toInstant()) && to.toInstant().equals(other.to.toInstant());
    }

    @Override
    public String toString() {
        return from + " -> " + to;
    }

}
