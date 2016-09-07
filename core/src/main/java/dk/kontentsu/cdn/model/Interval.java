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
package dk.kontentsu.cdn.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;


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

    /**
     * Returns true intervals share some part of the time-line.
     */
    public boolean overlaps(final Interval other) {
        if (other == null) {
            return false;
        } else {
            return other.equals(this)
                    || (from.compareTo(other.to) < 0 && other.from.compareTo(to) < 0);
        }
    }

    public Optional<Interval> intersection(final Interval other) {
        if (overlaps(other)) {
            ZonedDateTime newFrom = getFrom().isAfter(other.getFrom()) ? getFrom() : other.getFrom();
            ZonedDateTime newTo = getTo().isAfter(other.getTo()) ? other.getTo() : getTo();
            return Optional.of(new Interval(newFrom, newTo));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.from);
        hash = 59 * hash + Objects.hashCode(this.to);
        return hash;
    }

    private boolean equals(final Interval interval) {
        return from.toInstant().equals(interval.from.toInstant()) && to.toInstant().equals(interval.to.toInstant());
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
        return equals(other);
    }

    @Override
    public String toString() {
        return from + " -> " + to;
    }

}
