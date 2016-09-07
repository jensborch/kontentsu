package dk.kontentsu.cdn.model;

/**
 *
 * @author Jens Borch Christiansen
 */
public final class SemanticUriTaxonomy extends Taxonomy {

    public static final String NAME = "Semantic URI path";

    private static final long serialVersionUID = 5524732927983902692L;

    private static SemanticUriTaxonomy instance;

    private SemanticUriTaxonomy() {
    }

    public static synchronized SemanticUriTaxonomy create() {
        if (instance == null) {
            instance = new SemanticUriTaxonomy();
        }
        return instance;
    }

    @Override
    public void setName(final String name) {
        throw new UnsupportedOperationException("Name can not be changed");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this;
    }

    @Override
    public Integer getId() {
        return -1;
    }

}
