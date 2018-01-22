package dk.kontentsu.model;

import java.util.ArrayList;
import java.util.List;

import dk.kontentsu.model.processing.TemporalReferenceTree;
import dk.kontentsu.model.processing.TemporalReferenceTreeVisitor;
import dk.kontentsu.spi.ContentProcessingScoped;

@ContentProcessingScoped
public class TestVisitor implements TemporalReferenceTreeVisitor<TestVisitor.TestResults> {

    List<String> names = new ArrayList<>();

    @Override
    public TestResults getResults() {
        return new TestResults(names);
    }

    @Override
    public void visit(TemporalReferenceTree.Node node) {
        names.add(node.getVersion().getItem().getUri().getFileName());
    }

    public static class TestResults implements TemporalReferenceTreeVisitor.Results {

        List<String> names;

        public TestResults(List<String> names) {
            this.names = names;
        }

    }

}
