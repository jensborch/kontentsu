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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import dk.kontentsu.jpa.AbstractBaseEntity;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.State;
import dk.kontentsu.repository.Repository;

/**
 * A version of an item that can potentially be published to the CDN server. A
 * version is active during a time period defined by its interval. A version
 * (together with its content) is immutable except it can be marked as inactive.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "version")
@NamedQueries({
    @NamedQuery(name = Repository.VERSION_GET,
            query = "SELECT v FROM Version v WHERE v.uuid = :uuid")})
public class Version extends AbstractBaseEntity {

    private static final long serialVersionUID = 720940222528135649L;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> externalizationIds;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final List<Reference> references = new ArrayList<>();

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private Content content;

    @NotNull
    @Valid
    @Column(name = "interval")
    private Interval interval;

    @Column(name = "approver")
    private Approver approver;

    @NotNull
    @ManyToOne
    private Item item;

    @NotNull
    @Column(name = "state", length = 50)
    @Enumerated(EnumType.STRING)
    private State state;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "metadata",
            joinColumns = @JoinColumn(name = "version_id"),
            uniqueConstraints = {
                @UniqueConstraint(columnNames = {"version_id", "type", "key"})})
    private Map<Metadata.Key, Metadata> metadata;

    protected Version() {
        //Needed by JPA
    }

    private Version(final Builder builder) {
        setInterval(new Interval(builder.from, builder.to));
        this.externalizationIds = new HashSet<>();
        this.content = builder.content;
        this.state = builder.state;
        this.metadata = builder.metadata;
        if (builder.uuid != null) {
            setUuid(builder.uuid);
        }
    }

    private static Version create(final Builder builder) {
        Version result = new Version(builder);
        builder.references.stream().forEach((i) -> {
            result.references.add(new Reference(result, i.composition, i.type));
        });
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setApprover(final Approver approver) {
        this.approver = approver;
    }

    public Approver getApprover() {
        return approver;
    }

    public void addExternalizationId(final String id) {
        externalizationIds.add(id);
    }

    public void removeExternalizationId(final String id) {
        externalizationIds.remove(id);
    }

    public Set<String> getExternalizationIds() {
        return Collections.unmodifiableSet(externalizationIds);
    }

    void setItem(@Valid final Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public Map<Metadata.Key, Metadata> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public void delete() {
        state = State.DELETED;
    }

    public void activate() {
        state = State.ACTIVE;
    }

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    public boolean isDeleted() {
        return state == State.DELETED;
    }

    public State getState() {
        return state;
    }

    private void setInterval(@Valid final Interval interval) {
        this.interval = interval;
    }

    public Interval getInterval() {
        return interval;
    }

    public MimeType getMimeType() {
        return content.getMimeType();
    }

    public Content getContent() {
        return content;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(references.stream().map(c -> c.getItem()).collect(Collectors.toList()));
    }

    public boolean hasComposition() {
        return references.stream()
                .filter(c -> c.getType() == ReferenceType.COMPOSITION)
                .findAny()
                .isPresent();
    }

    public Map<SemanticUri, List<Version>> getComposition() {
        Map<SemanticUri, List<Version>> result = references.stream()
                .filter(c -> c.getType() == ReferenceType.COMPOSITION)
                .map(c -> c.getItem())
                .flatMap(i -> i.getVersions(interval).stream())
                .collect(Collectors.groupingBy(v -> v.item.getUri()));
        return result;
    }

    public List<Reference> getReferences() {
        return Collections.unmodifiableList(references);
    }

    public boolean isComplete() {
        return getReferences().isEmpty()
                || !getReferences().stream()
                        .filter(c -> c.getType() == ReferenceType.COMPOSITION)
                        .filter(c -> c.getItem().getVersions(interval).isEmpty())
                        .findAny().isPresent();
    }

    /**
     * Builder for composite version.
     */
    public static final class Builder {

        final Set<ItemCompositionType> references = new HashSet<>();
        private final Map<Metadata.Key, Metadata> metadata = new HashMap<>();

        private Content content;
        private ZonedDateTime from = ZonedDateTime.now();
        private ZonedDateTime to = Interval.INFINIT;
        private State state = State.ACTIVE;
        private UUID uuid;

        private Builder() {
        }

        public Builder version(final Version version) {
            this.references.addAll(version.getReferences()
                    .stream()
                    .map(c -> new ItemCompositionType(c.getItem(), c.getType()))
                    .collect(Collectors.toList()));
            this.content = version.getContent();
            this.to = version.getInterval().getTo();
            this.from = version.getInterval().getFrom();
            this.state = version.getState();
            this.uuid = UUID.randomUUID();
            this.metadata.putAll(version.metadata);
            return this;
        }

        public Builder active() {
            this.state = State.ACTIVE;
            return this;
        }

        public Builder from(final ZonedDateTime from) {
            this.from = from;
            return this;
        }

        public Builder to(final ZonedDateTime to) {
            this.to = to;
            return this;
        }

        public Builder interval(final Interval interval) {
            this.from = interval.getFrom();
            this.to = interval.getTo();
            return this;
        }

        public Builder draft() {
            this.state = State.DRAFT;
            return this;
        }

        public Builder reference(final Item reference, final ReferenceType type) {
            references.add(new ItemCompositionType(reference, type));
            return this;
        }

        public Builder content(final Content content) {
            this.content = content;
            return this;
        }

        public Builder metadata(final Metadata.Key key, final Metadata value) {
            metadata.put(key, value);
            return this;
        }

        public Version build() {
            return Version.create(this);
        }

        /**
         * Tuple containing an item and its composition type - useful for
         * building compositions.
         */
        public static class ItemCompositionType {

            Item composition;
            ReferenceType type;

            public ItemCompositionType(final Item composition, final ReferenceType type) {
                this.composition = composition;
                this.type = type;
            }
        }
    }
}
