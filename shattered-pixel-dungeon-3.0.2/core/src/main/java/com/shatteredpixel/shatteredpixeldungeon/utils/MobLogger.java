/*
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

package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.badlogic.gdx.Gdx;
import com.watabou.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MobLogger {

    private static final String LOG_FILE = "debug_logs/mob_events.txt";
    private static final String LOG_DIR  = "debug_logs/";

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);

    private static boolean appDirReady  = false;
    private static boolean localDirReady = false;

    // Called from Mob.onAdd() — fires exactly once per mob on first activation
    public static void logSpawn(int mobId, String mobType, int depth, long seed, int pos) {
        write(String.format(Locale.ROOT,
                "[%s] SPAWN      | id=%-4d type=%-18s depth=%-3d seed=%-15d pos=%d",
                timestamp(), mobId, mobType, depth, seed, pos));
    }

    // Called from Mob.act() when the AI state reference changes during a turn
    public static void logStateTransition(int mobId, String mobType, int depth,
                                          String fromState, String toState) {
        write(String.format(Locale.ROOT,
                "[%s] STATE      | id=%-4d type=%-18s depth=%-3d %s -> %s",
                timestamp(), mobId, mobType, depth, fromState, toState));
    }

    // Called from Mob.act() when justAlerted is true for this turn
    public static void logAlerted(int mobId, String mobType, int depth, int pos) {
        write(String.format(Locale.ROOT,
                "[%s] ALERTED    | id=%-4d type=%-18s depth=%-3d pos=%d",
                timestamp(), mobId, mobType, depth, pos));
    }

    // Called from Mob.act() when the target cell changes during a turn
    public static void logTargetChange(int mobId, String mobType, int depth,
                                       int oldTarget, int newTarget) {
        write(String.format(Locale.ROOT,
                "[%s] TARGET     | id=%-4d type=%-18s depth=%-3d target: %d -> %d",
                timestamp(), mobId, mobType, depth, oldTarget, newTarget));
    }

    private static void write(String entry) {
        // normal terminal output
        System.out.println(entry);

        // following normal conventions
        try {
            ensureAppDir();
            FileUtils.getFileHandle(LOG_FILE).writeString(entry + "\n", true);
        } catch (Exception e) {
            // swallow — logging must never disrupt gameplay
        }

        // specific logs to repo for assignment ease of showing
        try {
            ensureLocalDir();
            Gdx.files.local(LOG_FILE).writeString(entry + "\n", true);
        } catch (Exception e) {
            // don't interrupt gameplay - unhandled exceptions will crash the game
        }
    }

    private static void ensureAppDir() {
        if (!appDirReady) {
            FileUtils.getFileHandle(LOG_DIR).mkdirs();
            appDirReady = true;
        }
    }

    private static void ensureLocalDir() {
        if (!localDirReady) {
            Gdx.files.local(LOG_DIR).mkdirs();
            localDirReady = true;
        }
    }

    private static String timestamp() {
        return DATE_FORMAT.format(new Date());
    }
}
