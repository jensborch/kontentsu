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
package dk.kontentsu.api.exposure.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

import dk.kontentsu.api.exposure.CategoryExposure;
import dk.kontentsu.api.exposure.HostExposure;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.Provider;
import io.swagger.annotations.ApiModelProperty;

/**
 * REST representation of an item that can externalized to the CDN - i.e. a file on the CDN.
 *
 * @author Jens Borch Christiansen
 */
public class ItemRepresentation {

    @NotNull
    @ApiModelProperty(value = "The (semantic) URI of item on the CDN", dataType = "string", example = "pages/book/book.en.json", required = true)
    private final SemanticUri uri;

    @NotNull
    @ApiModelProperty(value = "The UUID of the item", example = "123e4567-e89b-12d3-a456-426655440000", required = true)
    private final UUID uuid;

    @NotNull
    @ApiModelProperty(value = "Additional taxonomy categories for the item", required = false)
    private final Map<String, List<Link>> categories;

    @ApiModelProperty(value = "The provider of the item", required = false)
    private final Provider provider;

    @NotNull
    @ApiModelProperty(value = "List of host the item should be published to", required = false)
    private final List<Link> hosts;

    @NotNull
    @ApiModelProperty(value = "Different versions of the item", required = true)
    private final List<VersionLinkRepresentation> versions;

    public ItemRepresentation(final Item from, final UriInfo uriInfo) {

        this.uri = from.getUri();
        this.uuid = from.getUuid();
        this.versions = from.getVersions()
                .stream()
                .map(v -> new VersionLinkRepresentation(v, uriInfo))
                .collect(Collectors.toList());

        this.provider = from.getProvider().orElse(null);

        this.categories = from.getCategories()
                .stream()
                .collect(Collectors.groupingBy(c -> c.getTaxonomy().getName(), Collectors.mapping(c -> Link
                        .fromUriBuilder(uriInfo.getBaseUriBuilder()
                                .path(CategoryExposure.class)
                                .path(CategoryExposure.class, "getCategory"))
                        .rel("category")
                        .title(c.toString())
                        .build(c.getTaxonomy().getName(), c.toString()), Collectors.toList())));

        this.hosts = from.getHosts().stream().map(h -> Link.fromUriBuilder(uriInfo.getBaseUriBuilder()
                .path(HostExposure.class)
                .path(HostExposure.class, "get"))
                .rel("host")
                .title(h.getName())
                .build(h.getName())).collect(Collectors.toList());
    }

    public UUID getUuid() {
        return uuid;
    }

    public SemanticUri getUri() {
        return uri;
    }

    public Map<String, List<Link>> getCategories() {
        return Collections.unmodifiableMap(categories);
    }

    public Provider getProvider() {
        return provider;
    }

    public List<Link> getHosts() {
        return hosts;
    }

    public List<VersionLinkRepresentation> getVersions() {
        return Collections.unmodifiableList(versions);
    }

}
