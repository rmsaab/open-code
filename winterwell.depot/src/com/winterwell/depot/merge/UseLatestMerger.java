package com.winterwell.depot.merge;

import java.util.Collection;

import com.winterwell.utils.Utils;

/**
 * The Latest value wins if set. i.e. latest + diff = latest, except null + diff = diff. 
 * @author daniel
 *
 */
public class UseLatestMerger extends AMerger<Object> {

	@Override
	public Diff diff(Object before, Object after) {
		return new Diff(UseLatestMerger.class, after);		
	}

	@Override
	public Object applyDiff(Object latest, Diff diff) {
		// latest wins if set
		if (latest==null) return diff.diff;
		if (latest instanceof CharSequence && ((CharSequence) latest).length()==0) {
			return diff.diff;
		}		
		return latest;
	}

	@Override
	public Object stripDiffs(Object v) {
		return v;
	}

}