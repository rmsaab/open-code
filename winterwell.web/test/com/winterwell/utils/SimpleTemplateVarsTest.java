package com.winterwell.utils;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.winterwell.utils.containers.ArrayMap;

public class SimpleTemplateVarsTest {

	@Test
	public void testProcessNoJS() {
		{
			Map src = new ArrayMap("name", "Daniel");
			SimpleTemplateVars props = new SimpleTemplateVars(src);
			String s = props.process("Hello $name! Leave $namething alone.");
			assertEquals("Hello Daniel! Leave $namething alone.", s);
		}
	}
	
	@Test
	public void testProcessScript() {
		{
			Map src = new ArrayMap("name", "Daniel");
			SimpleTemplateVars props = new SimpleTemplateVars(src).setUseJS(true);
			String s = props.process("Hello ${name}!");
			assertEquals("Hello Daniel!", s);
		}
		{	// ?
			Map src = new ArrayMap("name", "Daniel");
			SimpleTemplateVars props = new SimpleTemplateVars(src).setUseJS(true);
			String s = props.process("Hello ${window.name? name : ''}!");
			assertEquals("Hello Daniel!", s);
		}
		{	// unset!
			Map src = new ArrayMap("name", "Daniel");
			SimpleTemplateVars props = new SimpleTemplateVars(src).setUseJS(true);
			String s = props.process("Hello ${window.foo? foo : 'dev'}!");
			assertEquals("Hello dev!", s);
		}
	}
		

}
