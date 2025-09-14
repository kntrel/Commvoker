package com.kntrel.mc.commvoker.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequirementNodeTest {

    @Test
    void testAlwaysNode() {
        RequirementNode<Object> alwaysNode = RequirementNode.always();
        assertTrue(alwaysNode.test(new Object()));
    }

    @Test
    void testEmptyNode() {
        RequirementNode<Object> node = new RequirementNode<>();
        assertTrue(node.test(new Object())); // Should pass since no tests are added
    }

    @Test
    void testSinglePredicate() {
        RequirementNode<String> node = new RequirementNode<>();
        node.or(s -> s.startsWith("test"));
        assertTrue(node.test("testString"));
        assertFalse(node.test("example"));
    }

    @Test
    void testMultiplePredicates() {
        RequirementNode<Integer> node = new RequirementNode<>();
        node.or(i -> i > 10).or(i -> i % 2 == 0);
        assertTrue(node.test(12));
        assertTrue(node.test(15));
        assertFalse(node.test(9));
    }

    @Test
    void testOrAlways() {
        RequirementNode<String> node = new RequirementNode<>();
        node.or(s -> s.contains("pass")).orAlways();
        assertTrue(node.test("anything")); // Always passes after orAlways
    }

    @Test
    void testOrWithAnotherRequirementNode() {
        RequirementNode<String> node1 = new RequirementNode<>();
        node1.or(s -> s.startsWith("A"));

        RequirementNode<String> node2 = new RequirementNode<>();
        node2.or(s -> s.endsWith("Z"));

        node1.or(node2);
        assertTrue(node1.test("AZ"));
        assertTrue(node1.test("A"));
        assertTrue(node1.test("Z"));
        assertFalse(node1.test("B"));
    }

    @Test
    void testPassThroughBehavior() {
        RequirementNode<String> node = new RequirementNode<>();
        node.or(s -> s.equals("test")).orAlways();
        assertTrue(node.test("anything"));
    }

    @Test
    void testSingleAndPredicate() {
        RequirementNode<Integer> node = new RequirementNode<>();
        node.and(i -> i > 10);
        assertTrue(node.test(15));
        assertFalse(node.test(5));
    }

    @Test
    void testMultipleAndPredicates() {
        RequirementNode<Integer> node = new RequirementNode<>();
        node.and(i -> i > 10).and(i -> i % 2 == 0);
        assertTrue(node.test(12));
        assertFalse(node.test(15));
        assertFalse(node.test(8));
    }

    @Test
    void testAndWithAnotherRequirementNode() {
        RequirementNode<Integer> node1 = new RequirementNode<>();
        node1.and(i -> i > 10);

        RequirementNode<Integer> node2 = new RequirementNode<>();
        node2.and(i -> i % 2 == 0);

        node1.and(node2);
        assertTrue(node1.test(12));
        assertFalse(node1.test(15));
        assertFalse(node1.test(8));
    }

    @Test
    void testCombinationOfOrsAndAnds() {
        RequirementNode<Integer> node = new RequirementNode<>();
        node.or(i -> i > 10).or(i -> i % 2 == 0);
        node.and(i -> i < 20).and(i -> i != 15);

        assertTrue(node.test(12));
        assertFalse(node.test(22));
        assertFalse(node.test(15));
        assertFalse(node.test(9));
    }
}