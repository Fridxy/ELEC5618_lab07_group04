/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates game save bundles before restoring state, and logs any detected issues.
 * Used by Dungeon.loadGame() and Dungeon.loadLevel() to catch corruption early.
 */
public class SaveValidator {

	// Inclusive bounds for the dungeon depth field
	private static final int MIN_DEPTH = 1;
	private static final int MAX_DEPTH = 26;

	public static class ValidationResult {
		public final boolean isValid;
		public final List<String> issues;

		ValidationResult(boolean isValid, List<String> issues) {
			this.isValid = isValid;
			this.issues = issues;
		}
	}

	/**
	 * Validates the top-level game save bundle.
	 * Checks version, depth, branch, hero data, seed, gold, and energy.
	 */
	public static ValidationResult validateGameBundle(Bundle bundle) {
		List<String> issues = new ArrayList<>();

		if (bundle == null || bundle.isNull()) {
			issues.add("save bundle is null or empty");
			logIssues(issues);
			return new ValidationResult(false, issues);
		}

		int version = bundle.getInt(Dungeon.VERSION);
		if (version < ShatteredPixelDungeon.v2_3_2) {
			issues.add("version " + version + " is below minimum supported version " + ShatteredPixelDungeon.v2_3_2);
		}

		int depth = bundle.getInt("depth");
		if (depth < MIN_DEPTH || depth > MAX_DEPTH) {
			issues.add("depth " + depth + " is out of valid range [" + MIN_DEPTH + ", " + MAX_DEPTH + "]");
		}

		int branch = bundle.getInt("branch");
		if (branch < 0 || branch > 1) {
			issues.add("branch " + branch + " is invalid (expected 0 or 1)");
		}

		Bundle heroBundle = bundle.getBundle("hero");
		if (heroBundle == null || heroBundle.isNull()) {
			issues.add("hero data is missing");
		} else {
			int hp = heroBundle.getInt("HP");
			int ht = heroBundle.getInt("HT");
			if (ht <= 0) {
				issues.add("hero max HP (HT) is invalid: " + ht);
			}
			if (hp < 0) {
				issues.add("hero current HP is negative: " + hp);
			}
			if (ht > 0 && hp > ht) {
				issues.add("hero HP " + hp + " exceeds max HP " + ht);
			}
		}

		if (!bundle.contains("seed")) {
			issues.add("seed field is missing");
		}

		int gold = bundle.getInt("gold");
		if (gold < 0) {
			issues.add("gold is negative: " + gold);
		}

		int energy = bundle.getInt("energy");
		if (energy < 0) {
			issues.add("energy is negative: " + energy);
		}

		boolean valid = issues.isEmpty();
		if (!valid) {
			logIssues(issues);
		}
		return new ValidationResult(valid, issues);
	}

	/**
	 * Validates a depth/level save bundle.
	 * Confirms the bundle is non-null and contains the "level" key.
	 */
	public static ValidationResult validateLevelBundle(Bundle bundle) {
		List<String> issues = new ArrayList<>();

		if (bundle == null || bundle.isNull()) {
			issues.add("level bundle is null or empty");
			logIssues(issues);
			return new ValidationResult(false, issues);
		}

		if (!bundle.contains("level")) {
			issues.add("level key is missing from level data bundle");
			logIssues(issues);
			return new ValidationResult(false, issues);
		}

		return new ValidationResult(true, issues);
	}

	private static void logIssues(List<String> issues) {
		StringBuilder sb = new StringBuilder("[SaveValidator] Validation failed:");
		for (String issue : issues) {
			sb.append("\n  - ").append(issue);
		}
		Game.reportException(new SaveCorruptionException(sb.toString()));
	}

	/** Thrown (and immediately caught) to produce a labeled stack entry in crash logs. */
	public static class SaveCorruptionException extends RuntimeException {
		public SaveCorruptionException(String message) {
			super(message);
		}
	}
}
