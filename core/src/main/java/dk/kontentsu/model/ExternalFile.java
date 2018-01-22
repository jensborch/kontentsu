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

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import dk.kontentsu.repository.Repository;

/**
 * External file accessible on the CDN at a given point in time.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "external_file")
@NamedQueries({
    @NamedQuery(name = Repository.EXTERNAL_FILE_FIND_IN_INTERVAL,
            query = "SELECT f "
            + "FROM ExternalFile f "
            + "JOIN f.item it "
            + "JOIN it.path t "
            + "JOIN f.interval i "
            + "WHERE f.state = :state "
            + "AND ((:edition IS NULL AND it.edition IS NULL) OR it.edition = :edition) "
            + "AND t.path = :path "
            + "AND i.to >= :from AND i.from < :to"),
    @NamedQuery(name = Repository.EXTERNAL_FILE_FIND_ALL_IN_INTERVAL,
            query = "SELECT f "
            + "FROM ExternalFile f "
            + "JOIN f.interval i "
            + "WHERE f.state = :state "
            + "AND i.to >= :from AND i.from < :to"),
    @NamedQuery(name = Repository.EXTERNAL_FILE_SCHEDULE,
            query = "SELECT DISTINCT f.interval "
            + "FROM ExternalFile f "
            + "JOIN f.interval i "
            + "WHERE f.state = :state "
            + "AND i.from >= :from OR i.to > :from"),
    @NamedQuery(name = Repository.EXTERNAL_FILE_FIND_ALL,
            query = "SELECT f FROM ExternalFile f WHERE f.state = :state"),
    @NamedQuery(name = Repository.EXTERNAL_FILE_FIND_ALL_AT_DATE_TIME,
            query = "SELECT DISTINCT f FROM ExternalFile f "
            + "JOIN f.interval i "
            + "WHERE f.state = :state "
            + "AND i.from <= :at AND i.to > :at"),
    @NamedQuery(name = Repository.EXTERNAL_FILE_GET,
            query = "SELECT f FROM ExternalFile f WHERE f.uuid = :uuid"),
    @NamedQuery(name = Repository.EXTERNAL_FILE_FIND_BY_URI,
            query = "SELECT f FROM ExternalFile f "
            + "JOIN f.item it "
            + "JOIN it.path t "
            + "JOIN f.interval i "
            + "WHERE f.state = :state "
            + "AND ((:edition IS NULL AND it.edition IS NULL) OR it.edition = :edition) "
            + "AND t.path = :path "
            + "AND i.from <= :at AND i.to > :at")})
public class ExternalFile extends AbstractBaseEntity {

    private static final long serialVersionUID = 857462851260372880L;

    @Column(name = "externalization_id", unique = true)
    private String externalizationId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private Content content;

    @NotNull
    @Column(name = "interval")
    private Interval interval;

    @NotNull
    @Column(name = "state")
    private State state;

    protected ExternalFile() {
        //Needed by JPA
    }

    private ExternalFile(final Builder builder) {
        this.item = builder.item;
        this.content = builder.content;
        this.interval = new Interval(builder.from, builder.to);
        this.state = builder.state;
        this.externalizationId = builder.externalizationId;
        if (builder.uuid != null) {
            setUuid(builder.uuid);
        }
        item.addFile(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getExternalizationId() {
        return externalizationId;
    }

    public Item getItem() {
        return item;
    }

    public void delete() {
        state = State.DELETED;
    }

    public void activate() {
        state = State.ACTIVE;
    }

    public boolean isActivate() {
        return state == State.ACTIVE;
    }

    public boolean isDeleted() {
        return state == State.DELETED;
    }

    public State getState() {
        return state;
    }

    public Interval getInterval() {
        return interval;
    }

    public Content getContent() {
        return content;
    }

    public Path resolvePath(final Path path) {
        return path.resolve(getItem().getUri().toPath());
    }

    public boolean isDifferent(final Version version) {
        return !version.getExternalizationIds().contains(externalizationId);
    }

    public MimeType getMimeType() {
        return item.getMimeType();
    }

    /**
     * Builder for composite version.
     */
    public static final class Builder {

        private String externalizationId;
        private Item item;
        private Content content;
        private ZonedDateTime from = ZonedDateTime.now();
        private ZonedDateTime to = Interval.INFINITE;
        private State state = State.ACTIVE;
        private UUID uuid;

        private Builder() {
        }

        public Builder externalFile(final ExternalFile file) {
            this.item = file.getItem();
            this.content = file.getContent();
            this.to = file.getInterval().getTo();
            this.from = file.getInterval().getFrom();
            this.state = file.getState();
            this.uuid = file.getUuid();
            this.externalizationId = file.getExternalizationId();
            return this;
        }

        public Builder item(final Item item) {
            this.item = item;
            return this;
        }

        public Builder from(final ZonedDateTime from) {
            this.from = from;
            return this;
        }

        @SuppressWarnings("PMD.ShortMethodName")
        public Builder to(final ZonedDateTime to) {
            this.to = to;
            return this;
        }

        public Builder interval(final Interval interval) {
            this.to = interval.getTo();
            this.from = interval.getFrom();
            return this;
        }

        public Builder state(final State state) {
            this.state = state;
            return this;
        }

        public Builder content(final Content content) {
            this.content = content;
            return this;
        }

        public Builder externalizationId(final String externalizationId) {
            this.externalizationId = externalizationId;
            return this;
        }

        public ExternalFile build() {
            return new ExternalFile(this);
        }
    }
}
