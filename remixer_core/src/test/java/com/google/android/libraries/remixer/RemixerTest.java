/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.libraries.remixer;

import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RemixerTest {

  private Remixer remixer;
  private Variable variable;
  private Variable variable2;

  /**
   * Sets up the tests.
   */
  @Before
  public void setUp() {
    variable = new Variable<>("name", "key", "", this, null, 0);
    variable.init();
    variable2 = new Variable<>("name2", "key2", "", this, null, 0);
    variable2.init();
    remixer = new Remixer();
  }

  @Test(expected = DuplicateKeyException.class)
  public void remixerRejectsDuplicatesForSamecontext() {
    remixer.addItem(variable);
    remixer.addItem(variable);
  }

  @Test(expected = IncompatibleRemixerItemsWithSameKeyException.class)
  public void remixerRejectsDuplicatesWithDifferentTypes() {
    final Object context1 = new Object();
    final Object context2 = new Object();
    final Variable<String> variableString = new Variable<>("name", "key", "", context1, null, 0);
    final Variable<Boolean> variableBoolean =
        new Variable<>("name", "key", false, context2, null, 0);

    remixer.addItem(variableString);
    remixer.addItem(variableBoolean);
  }

  @Test(expected = IncompatibleRemixerItemsWithSameKeyException.class)
  public void remixerRejectsDuplicatesOneVariableOneTrigger() {
    final Object context1 = new Object();
    final Object context2 = new Object();
    final Variable<String> variableString = new Variable<>("name", "key", "", context1, null, 0);
    final Trigger trigger = new Trigger("name", "key", context2, null, 0);

    remixer.addItem(variableString);
    remixer.addItem(trigger);
  }

  @Test(expected = IncompatibleRemixerItemsWithSameKeyException.class)
  public void remixerRejectsDuplicatesOneVariableOneTriggerAftercontextReclaimed() {
    final Object context1 = new Object();
    final Object context2 = new Object();
    final Variable<String> variableString = new Variable<>("name", "key", "", context1, null, 0);
    final Trigger trigger = new Trigger("name", "key", context2, null, 0);

    remixer.addItem(variableString);
    // Simulate context reclaimed.
    variableString.clearContext();
    variableString.clearCallback();
    remixer.addItem(trigger);
  }

  @Test(expected = IncompatibleRemixerItemsWithSameKeyException.class)
  public void remixerRejectsDuplicatesWithDifferentTypesAftercontextReclaimed() {
    final Object context1 = new Object();
    final Object context2 = new Object();
    final Variable<String> variableString = new Variable<>("name", "key", "", context1, null, 0);
    final Variable<Boolean> variableBoolean =
        new Variable<>("name", "key", false, context2, null, 0);

    remixer.addItem(variableString);
    // Simulate context reclaimed.
    variableString.clearContext();
    variableString.clearCallback();
    remixer.addItem(variableBoolean);
  }

  /**
   * Replacement should only happen if the first context has been reclaimed and the remixer
   * item being added has a context of the same class.
   */
  public void remixerReplacesVariableCorrectly() {
    // Initialize two nearly identical variables with two different contexts of the same class
    final Object context1 = new Object();
    final Object context2 = new Object();
    final Variable<String> variableString = new Variable<>("name", "key", "", context1, null, 0);
    final Variable<String> variableString2 = new Variable<>("name", "key", "", context2, null, 0);

    // Add the first.
    remixer.addItem(variableString);
    // Simulate the first context is reclaimed.
    variableString.clearContext();
    variableString.clearCallback();
    // Add the second, since the first object was "reclaimed" and they are compatible, the first
    // should be removed, and the second should be kept.
    remixer.addItem(variableString2);
    List<RemixerItem> list = remixer.getRemixerItems();
    Assert.assertEquals(1, list.size());
    Assert.assertEquals(variable2, list.get(0));
  }

  @Test
  public void remixerUpdatesVariableValueWhenJustAdded() {
    // Initialize two nearly identical variables with two different contexts of the same class
    final Object context1 = new Object();
    final Object context2 = new Object();
    final Variable<String> variableString = new Variable<>("name", "key", "", context1, null, 0);
    final Variable<String> variableString2 = new Variable<>("name", "key", "", context2, null, 0);
    remixer.addItem(variableString);
    variableString.setValue("May the force be with you");
    Assert.assertEquals("May the force be with you", variableString.getSelectedValue());
    remixer.addItem(variableString2);
    Assert.assertEquals(variableString.getSelectedValue(), variableString2.getSelectedValue());
  }

  @Test
  public void remixerUpdatesAllExistingVariableValuesWhenAnyOfThemChanges() {
    // Initialize two nearly identical variables with two different contexts of the same class
    final Object context1 = new Object();
    final Object context2 = new Object();
    final Variable<String> variableString = new Variable<>("name", "key", "", context1, null, 0);
    final Variable<String> variableString2 = new Variable<>("name", "key", "", context2, null, 0);
    remixer.addItem(variableString);
    remixer.addItem(variableString2);
    variableString.setValue("May the force be with you");
    Assert.assertEquals("May the force be with you", variableString.getSelectedValue());
    Assert.assertEquals(variableString.getSelectedValue(), variableString2.getSelectedValue());
  }

  @Test
  public void remixerReturnsListInOrder() {
    remixer.addItem(variable);
    remixer.addItem(variable2);
    List<RemixerItem> remixerItemList = remixer.getRemixerItems();
    Assert.assertEquals(variable, remixerItemList.get(0));
    Assert.assertEquals(variable2, remixerItemList.get(1));
  }
}
