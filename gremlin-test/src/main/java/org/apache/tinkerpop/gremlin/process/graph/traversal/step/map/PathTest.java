/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.graph.traversal.step.map;

import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import org.apache.tinkerpop.gremlin.process.Path;
import org.apache.tinkerpop.gremlin.process.Traversal;
import org.apache.tinkerpop.gremlin.process.TraversalEngine;
import org.apache.tinkerpop.gremlin.process.UseEngine;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.apache.tinkerpop.gremlin.process.graph.traversal.__.*;
import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public abstract class PathTest extends AbstractGremlinProcessTest {

    public abstract Traversal<Vertex, Path> get_g_VX1X_name_path(final Object v1Id);

    public abstract Traversal<Vertex, Path> get_g_VX1X_out_path_byXageX_byXnameX(final Object v1Id);

    public abstract Traversal<Vertex, Path> get_g_V_repeatXoutX_timesX2X_path_by_byXnameX_byXlangX();

    public abstract Traversal<Vertex, Path> get_g_V_out_out_path_byXnameX_byXageX();

    public abstract Traversal<Vertex, Path> get_g_V_asXaX_hasXname_markoX_asXbX_hasXage_29X_asXcX_path();

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_name_path() {
        final Traversal<Vertex, Path> traversal = get_g_VX1X_name_path(convertToVertexId("marko"));
        printTraversalForm(traversal);
        final Path path = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(2, path.size());
        assertEquals(convertToVertexId("marko"), ((Vertex) path.get(0)).<String>id());
        assertEquals("marko", path.<String>get(1));
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_out_path_byXageX_byXnameX() {
        final Traversal<Vertex, Path> traversal = get_g_VX1X_out_path_byXageX_byXnameX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        int counter = 0;
        final Set<String> names = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            final Path path = traversal.next();
            assertEquals(Integer.valueOf(29), path.<Integer>get(0));
            assertTrue(path.get(1).equals("josh") || path.get(1).equals("vadas") || path.get(1).equals("lop"));
            names.add(path.get(1));
        }
        assertEquals(3, counter);
        assertEquals(3, names.size());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXoutX_timesX2X_path_byXitX_byXnameX_byXlangX() {
        final Traversal<Vertex, Path> traversal = get_g_V_repeatXoutX_timesX2X_path_by_byXnameX_byXlangX();
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            final Path path = traversal.next();
            assertEquals(3, path.size());
            assertEquals("marko", ((Vertex) path.get(0)).<String>value("name"));
            assertEquals("josh", path.<String>get(1));
            assertEquals("java", path.<String>get(2));
        }
        assertEquals(2, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_out_out_path_byXnameX_byXageX() {
        final Traversal<Vertex, Path> traversal = get_g_V_out_out_path_byXnameX_byXageX();
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            final Path path = traversal.next();
            assertEquals(3, path.size());
            assertEquals("marko", path.<String>get(0));
            assertEquals(Integer.valueOf(32), path.<Integer>get(1));
            assertTrue(path.get(2).equals("lop") || path.get(2).equals("ripple"));
        }
        assertEquals(2, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_asXaX_hasXname_markoX_asXbX_hasXage_29X_asXcX_path() {
        final Traversal<Vertex, Path> traversal = get_g_V_asXaX_hasXname_markoX_asXbX_hasXage_29X_asXcX_path();
        printTraversalForm(traversal);
        final Path path = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(1, path.size());
        assertTrue(path.hasLabel("a"));
        assertTrue(path.hasLabel("b"));
        assertTrue(path.hasLabel("c"));
        assertEquals(1, path.labels().size());
        assertEquals(3, path.labels().get(0).size());
    }

    @UseEngine(TraversalEngine.Type.STANDARD)
    public static class StandardTest extends PathTest {
        @Override
        public Traversal<Vertex, Path> get_g_VX1X_name_path(final Object v1Id) {
            return g.V(v1Id).values("name").path();
        }

        @Override
        public Traversal<Vertex, Path> get_g_VX1X_out_path_byXageX_byXnameX(final Object v1Id) {
            return g.V(v1Id).out().path().by("age").by("name");
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_repeatXoutX_timesX2X_path_by_byXnameX_byXlangX() {
            return g.V().repeat(out()).times(2).path().by().by("name").by("lang");
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_out_out_path_byXnameX_byXageX() {
            return g.V().out().out().path().by("name").by("age");
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_asXaX_hasXname_markoX_asXbX_hasXage_29X_asXcX_path() {
            return g.V().as("a").has("name", "marko").as("b").has("age", 29).as("c").path();
        }
    }

    @UseEngine(TraversalEngine.Type.COMPUTER)
    public static class ComputerTest extends StandardTest {
    }
}