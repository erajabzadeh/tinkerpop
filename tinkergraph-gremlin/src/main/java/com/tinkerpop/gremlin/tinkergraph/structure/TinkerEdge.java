package com.tinkerpop.gremlin.tinkergraph.structure;

import com.tinkerpop.gremlin.process.Step;
import com.tinkerpop.gremlin.process.TraversalEngine;
import com.tinkerpop.gremlin.process.computer.GraphComputer;
import com.tinkerpop.gremlin.process.graph.DefaultGraphTraversal;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.filter.HasStep;
import com.tinkerpop.gremlin.process.graph.map.IdentityStep;
import com.tinkerpop.gremlin.process.graph.map.StartStep;
import com.tinkerpop.gremlin.process.util.TraversalHelper;
import com.tinkerpop.gremlin.structure.Compare;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.structure.util.HasContainer;
import com.tinkerpop.gremlin.structure.util.StringFactory;
import com.tinkerpop.gremlin.tinkergraph.process.graph.map.TinkerGraphStep;
import com.tinkerpop.gremlin.tinkergraph.process.graph.util.optimizers.TinkerGraphStepOptimizer;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class TinkerEdge extends TinkerElement implements Edge {

    private final Vertex inVertex;
    private final Vertex outVertex;

    protected TinkerEdge(final Object id, final Vertex outVertex, final String label, final Vertex inVertex, final TinkerGraph graph) {
        super(id, label, graph);
        this.outVertex = outVertex;
        this.inVertex = inVertex;
        this.graph.edgeIndex.autoUpdate(Element.LABEL, this.label, null, this);
    }

    public <V> Property<V> setProperty(final String key, final V value) {
        if (this.graph.useGraphView) {
            return this.graph.graphView.setProperty(this, key, value);
        } else {
            ElementHelper.validateProperty(key, value);
            final Property oldProperty = super.getProperty(key);
            final Property newProperty = new TinkerProperty<>(this, key, value);
            this.properties.put(key, newProperty);
            this.graph.edgeIndex.autoUpdate(key, value, oldProperty.isPresent() ? oldProperty.get() : null, this);
            return newProperty;
        }
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        if (direction.equals(Direction.IN))
            return this.inVertex;
        else if (direction.equals(Direction.OUT))
            return this.outVertex;
        else
            throw Element.Exceptions.bothIsNotSupported();
    }

    public void remove() {
        if (!this.graph.edges.containsKey(this.getId()))
            throw Element.Exceptions.elementHasAlreadyBeenRemovedOrDoesNotExist(Edge.class, this.getId());

        final TinkerVertex outVertex = (TinkerVertex) this.getVertex(Direction.OUT);
        final TinkerVertex inVertex = (TinkerVertex) this.getVertex(Direction.IN);
        if (null != outVertex && null != outVertex.outEdges) {
            final Set<Edge> edges = outVertex.outEdges.get(this.getLabel());
            if (null != edges)
                edges.remove(this);
        }
        if (null != inVertex && null != inVertex.inEdges) {
            final Set<Edge> edges = inVertex.inEdges.get(this.getLabel());
            if (null != edges)
                edges.remove(this);
        }

        this.graph.edgeIndex.removeElement(this);
        this.graph.edges.remove(this.getId());
        this.properties.clear();
    }

    public String toString() {
        return StringFactory.edgeString(this);

    }

    //////

    public GraphTraversal<Edge, Edge> start() {
        final TinkerEdge edge = this;
        final GraphTraversal<Edge, Edge> traversal = new DefaultGraphTraversal<Edge, Edge>() {
            public GraphTraversal<Edge, Edge> submit(final TraversalEngine engine) {
                if (engine instanceof GraphComputer) {
                    this.optimizers().unregister(TinkerGraphStepOptimizer.class);
                    final String label = this.getSteps().get(0).getAs();
                    TraversalHelper.removeStep(0, this);
                    final Step identityStep = new IdentityStep(this);
                    if (TraversalHelper.isLabeled(label))
                        identityStep.setAs(label);

                    TraversalHelper.insertStep(identityStep, 0, this);
                    TraversalHelper.insertStep(new HasStep(this, new HasContainer(Element.ID, Compare.EQUAL, edge.getId())), 0, this);
                    TraversalHelper.insertStep(new TinkerGraphStep<>(this, Edge.class, edge.graph), 0, this);
                }
                return super.submit(engine);
            }
        };
        return (GraphTraversal) traversal.addStep(new StartStep<>(traversal, this));
    }
}
