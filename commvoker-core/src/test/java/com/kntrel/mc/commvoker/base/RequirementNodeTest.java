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
        assertTrue(node1.test("AZ")); // Matches both predicates
        assertTrue(node1.test("A")); // Matches first predicate
        assertTrue(node1.test("Z")); // Matches second predicate
        assertFalse(node1.test("B")); // Matches neither
    }

    @Test
    void testPassThroughBehavior() {
        RequirementNode<String> node = new RequirementNode<>();
        node.or(s -> s.equals("test")).orAlways();
        assertTrue(node.test("anything")); // Pass-through is enabled
    }
}