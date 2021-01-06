package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.*;

public class GlyphLayout4 extends GlyphLayout {
	private final Array<Color> colorStack = new Array(4);

	private float modkerning;
	private float modLineHeight = 0;
	private float glyWidth=0;

	public void setText (BitmapFont font, CharSequence str) {
		setText(font, str, 0, str.length(), font.getColor(), 0, Align.left, false, null);
	}

	public void setText (BitmapFont font, CharSequence str, Color color, float targetWidth, int halign, boolean wrap) {
		setText(font, str, 0, str.length(), color, targetWidth, halign, wrap, null);
	}

	public void setText (BitmapFont font, CharSequence str, int start, int end, Color color, float targetWidth, int halign,
                         boolean wrap, String truncate) {

		if (truncate != null)
			wrap = true; // Causes truncate code to run, doesn't actually cause wrapping.
//		else if (targetWidth <= font.getData().spaceXadvance) //
		else if (targetWidth <= font.getData().spaceWidth) //
			wrap = false; // Avoid one line per character, which is very inefficient.

		BitmapFont.BitmapFontData fontData = font.getData();
		boolean markupEnabled = fontData.markupEnabled;

		Pool<GlyphRun> glyphRunPool = Pools.get(GlyphRun.class);
		Array<GlyphRun> runs1 = this.runs;
		glyphRunPool.freeAll(runs1);
		runs1.clear();

		float x = 0, y = 0, width1 = 0;
		int lines = 0, blankLines = 0;


		Array<Color> colorStack = this.colorStack;
		Color nextColor = color;
		colorStack.add(color);
		Pool<Color> colorPool = Pools.get(Color.class);

		int runStart = start;
		outer:
		while (true) {
			// Each run is delimited by newline or left square bracket.
			int runEnd = -1;
			boolean newline = false, colorRun = false;
			if (start == end) {
				if (runStart == end) break; // End of string with no run to process, we're done.
				runEnd = end; // End of string, process last run.
			} else {
				switch (str.charAt(start++)) {
					case '\n':
						// End of line.
						runEnd = start - 1;
						newline = true;
						break;
					case '[':
						// Possible color tag.
						if (markupEnabled) {
							int length = parseColorMarkup(str, start, end, colorPool);
							if (length >= 0) {
								runEnd = start - 1;
								start += length + 1;
								nextColor = colorStack.peek();
								colorRun = true;
							} else if (length == -2) {
								start++; // Skip first of "[[" escape sequence.
								continue outer;
							}
						}
						break;
				}
			}

			if (runEnd != -1) {
				if (runEnd != runStart) { // Can happen (eg) when a color tag is at text start or a line is "\n".
					// Store the run that has ended.
					GlyphRun run = glyphRunPool.obtain();
					run.color.set(color);
					run.x = x;
					run.y = y;
					getGlyphs(str, fontData, runStart, runEnd, colorRun, run);
					if (run.glyphs.size == 0)
						glyphRunPool.free(run);
					else {
						runs1.add(run);

						// Compute the run width, wrap if necessary, and position the run.
						float[] xAdvances = run.xAdvances.items;
						for (int i = 0, n = run.xAdvances.size; i < n; i++) {
							float xAdvance = xAdvances[i];
							x += xAdvance;

							// Don't wrap if the glyph would fit with just its width (no xadvance or kerning).
							if (wrap && x > targetWidth && i > 1
									&& x - xAdvance + (run.glyphs.get(i - 1).xoffset + run.glyphs.get(i - 1).width) * fontData.scaleX
									- 0.0001f > targetWidth) {

								if (truncate != null) {
									truncate(fontData, run, targetWidth, truncate, i, glyphRunPool);
									x = run.x + run.width;
									break outer;
								}

								int wrapIndex = fontData.getWrapIndex(run.glyphs, i);
								if ((run.x == 0 && wrapIndex == 0) // Require at least one glyph per line.
										|| wrapIndex >= run.glyphs.size) { // Wrap at least the glyph that didn't fit.
									wrapIndex = i - 1;
								}
								GlyphRun next;
								if (wrapIndex == 0)
									next = run; // No wrap index, move entire run to next line.
								else {
									next = wrap(fontData, run, glyphRunPool, wrapIndex, i);
									runs1.add(next);
								}

								// Start the loop over with the new run on the next line.
								width1 = Math.max(width1, run.x + run.width);
								x = 0;
								y += fontData.down - modLineHeight;
								lines++;
								next.x = 0;
								next.y = y;
								i = -1;
								n = next.xAdvances.size;
								xAdvances = next.xAdvances.items;
								run = next;
							} else
								run.width += xAdvance;
						}
					}
				}

				if (newline) {
					// Next run will be on the next line.
					width1 = Math.max(width1, x);
					x = 0;
					float down = fontData.down;
					if (runEnd == runStart) { // Blank line.
						down *= fontData.blankLineScale;
						blankLines++;
					} else
						lines++;
					y += down;

					y -= modLineHeight;
				}

				runStart = start;
				color = nextColor;
			}
		}
		width1 = Math.max(width1, x);

		for (int i = 1, n = colorStack.size; i < n; i++)
			colorPool.free(colorStack.get(i));
		colorStack.clear();

		// Align runs to center or right of targetWidth.
		if ((halign & Align.left) == 0) { // Not left aligned, so must be center or right aligned.
			boolean center = (halign & Align.center) != 0;
			float lineWidth = 0, lineY = Integer.MIN_VALUE;
			int lineStart = 0, n = runs1.size;
			for (int i = 0; i < n; i++) {
				GlyphRun run = runs1.get(i);
				if (run.y != lineY) {
					lineY = run.y;
					float shift = targetWidth - lineWidth;
					if (center) shift /= 2;
					while (lineStart < i)
						runs1.get(lineStart++).x += shift;
					lineWidth = 0;
				}
				lineWidth += run.width;
			}
			float shift = targetWidth - lineWidth;
			if (center) shift /= 2;
			while (lineStart < n)
				runs1.get(lineStart++).x += shift;
		}

		this.width = width1;
		if (str.length()>10) {
			glyWidth=width1;
		}
		this.height =  (lines + blankLines) * modLineHeight + fontData.capHeight + lines * fontData.lineHeight + blankLines * fontData.lineHeight * fontData.blankLineScale;
		this.lines = lines;
	}

	public float getLayout4Width() {
		return glyWidth;
	}
	private void getGlyphs (CharSequence str, BitmapFont.BitmapFontData fontData, int runStart, int runEnd, boolean colorRun,
                            GlyphRun run) {
		boolean markupEnabled = fontData.markupEnabled;
		float scaleX = fontData.scaleX;
		BitmapFont.Glyph missingGlyph = fontData.missingGlyph;
		Array<BitmapFont.Glyph> glyphs = run.glyphs;
		FloatArray xAdvances = run.xAdvances;

		// Guess at number of glyphs needed.
		glyphs.ensureCapacity(runEnd - runStart);
		xAdvances.ensureCapacity(runEnd - runStart + 1);

		BitmapFont.Glyph lastGlyph = null;
		while (runStart < runEnd) {
			char ch = str.charAt(runStart++);
			BitmapFont.Glyph glyph = fontData.getGlyph(ch);
			if (glyph == null) {
				if (missingGlyph == null) continue;
				glyph = missingGlyph;
			}

			glyphs.add(glyph);

			if (lastGlyph == null) // First glyph.
				xAdvances.add((!colorRun || glyph.fixedWidth) ? 0 : -glyph.xoffset * scaleX - fontData.padLeft);
			else
				xAdvances.add((lastGlyph.xadvance + modkerning + lastGlyph.getKerning(ch)) * scaleX);
			lastGlyph = glyph;

			// "[[" is an escaped left square bracket, skip second character.
			if (markupEnabled && ch == '[' && runStart < runEnd && str.charAt(runStart) == '[') runStart++;
		}
		if (lastGlyph != null) {
			float lastGlyphWidth = (!colorRun || lastGlyph.fixedWidth) ? lastGlyph.xadvance + modkerning
					: lastGlyph.xoffset + lastGlyph.width - fontData.padRight;
			xAdvances.add(lastGlyphWidth * scaleX);
		}
	}

	private void truncate (BitmapFont.BitmapFontData fontData, GlyphRun run, float targetWidth, String truncate, int widthIndex,
                           Pool<GlyphRun> glyphRunPool) {

		// Determine truncate string size.
		GlyphRun truncateRun = glyphRunPool.obtain();
		getGlyphs(truncate, fontData, 0, truncate.length(), true, truncateRun);
		float truncateWidth = 0;
		for (int i = 1, n = truncateRun.xAdvances.size; i < n; i++)
			truncateWidth += truncateRun.xAdvances.get(i);
		targetWidth -= truncateWidth;

		// Determine visible glyphs.
		int count = 0;
		float width = run.x;
		while (count < run.xAdvances.size) {
			float xAdvance = run.xAdvances.get(count);
			width += xAdvance;
			if (width > targetWidth) {
				run.width = width - run.x - xAdvance;
				break;
			}
			count++;
		}

		if (count > 1) {
			// Some run glyphs fit, append truncate glyphs.
			run.glyphs.truncate(count - 1);
			run.xAdvances.truncate(count);
			adjustLastGlyph(fontData, run);
			if (truncateRun.xAdvances.size > 0) run.xAdvances.addAll(truncateRun.xAdvances, 1, truncateRun.xAdvances.size - 1);
		} else {
			// No run glyphs fit, use only truncate glyphs.
			run.glyphs.clear();
			run.xAdvances.clear();
			run.xAdvances.addAll(truncateRun.xAdvances);
			if (truncateRun.xAdvances.size > 0) run.width += truncateRun.xAdvances.get(0);
		}
		run.glyphs.addAll(truncateRun.glyphs);
		run.width += truncateWidth;

		glyphRunPool.free(truncateRun);
	}

	private GlyphRun wrap (BitmapFont.BitmapFontData fontData, GlyphRun first, Pool<GlyphRun> glyphRunPool, int wrapIndex, int widthIndex) {
		GlyphRun second = glyphRunPool.obtain();
		second.color.set(first.color);
		int glyphCount = first.glyphs.size;

		// Increase first run width up to the end index.
		while (widthIndex < wrapIndex)
			first.width += first.xAdvances.get(widthIndex++);

		// Reduce first run width by the wrapped glyphs that have contributed to the width.
		while (widthIndex > wrapIndex + 1)
			first.width -= first.xAdvances.get(--widthIndex);

		// Copy wrapped glyphs and xAdvances to second run.
		// The second run will contain the remaining glyph data, so swap instances rather than copying to reduce large allocations.
		if (wrapIndex < glyphCount) {
			Array<BitmapFont.Glyph> glyphs1 = second.glyphs; // Starts empty.
			Array<BitmapFont.Glyph> glyphs2 = first.glyphs; // Starts with all the glyphs.
			glyphs1.addAll(glyphs2, 0, wrapIndex);
			glyphs2.removeRange(0, wrapIndex - 1);
			first.glyphs = glyphs1;
			second.glyphs = glyphs2;
			// Equivalent to:
			// second.glyphs.addAll(first.glyphs, wrapIndex, glyphCount - wrapIndex);
			// first.glyphs.truncate(wrapIndex);

			FloatArray xAdvances1 = second.xAdvances; // Starts empty.
			FloatArray xAdvances2 = first.xAdvances; // Starts with all the xAdvances.
			xAdvances1.addAll(xAdvances2, 0, wrapIndex + 1);
			xAdvances2.removeRange(1, wrapIndex); // Leave first entry to be overwritten by next line.
			xAdvances2.set(0, -glyphs2.first().xoffset * fontData.scaleX - fontData.padLeft);
			first.xAdvances = xAdvances1;
			second.xAdvances = xAdvances2;
			// Equivalent to:
			// second.xAdvances.add(-second.glyphs.first().xoffset * fontData.scaleX - fontData.padLeft);
			// second.xAdvances.addAll(first.xAdvances, wrapIndex + 1, first.xAdvances.size - (wrapIndex + 1));
			// first.xAdvances.truncate(wrapIndex + 1);
		}

		if (wrapIndex == 0) {
			// If the first run is now empty, remove it.
			glyphRunPool.free(first);
			runs.pop();
		} else
			adjustLastGlyph(fontData, first);

		return second;
	}

	/** Adjusts the xadvance of the last glyph to use its width instead of xadvance. */
	private void adjustLastGlyph (BitmapFont.BitmapFontData fontData, GlyphRun run) {
		BitmapFont.Glyph last = run.glyphs.peek();
		if (fontData.isWhitespace((char)last.id)) return; // Can happen when doing truncate.
		float width = (last.xoffset + last.width) * fontData.scaleX - fontData.padRight;
		run.width += width - run.xAdvances.peek(); // Can cause the run width to be > targetWidth, but the problem is minimal.
		run.xAdvances.set(run.xAdvances.size - 1, width);
	}

	private int parseColorMarkup (CharSequence str, int start, int end, Pool<Color> colorPool) {
		if (start == end) return -1; // String ended with "[".
		switch (str.charAt(start)) {
			case '#':
				// Parse hex color RRGGBBAA where AA is optional and defaults to 0xFF if less than 6 chars are used.
				int colorInt = 0;
				for (int i = start + 1; i < end; i++) {
					char ch = str.charAt(i);
					if (ch == ']') {
						if (i < start + 2 || i > start + 9) break; // Illegal number of hex digits.
						if (i - start <= 7) { // RRGGBB or fewer chars.
							for (int ii = 0, nn = 9 - (i - start); ii < nn; ii++)
								colorInt = colorInt << 4;
							colorInt |= 0xff;
						}
						Color color = colorPool.obtain();
						colorStack.add(color);
						Color.rgba8888ToColor(color, colorInt);
						return i - start;
					}
					if (ch >= '0' && ch <= '9')
						colorInt = colorInt * 16 + (ch - '0');
					else if (ch >= 'a' && ch <= 'f')
						colorInt = colorInt * 16 + (ch - ('a' - 10));
					else if (ch >= 'A' && ch <= 'F')
						colorInt = colorInt * 16 + (ch - ('A' - 10));
					else
						break; // Unexpected character in hex color.
				}
				return -1;
			case '[': // "[[" is an escaped left square bracket.
				return -2;
			case ']': // "[]" is a "pop" color tag.
				if (colorStack.size > 1) colorPool.free(colorStack.pop());
				return 0;
		}
		// Parse named color.
		int colorStart = start;
		for (int i = start + 1; i < end; i++) {
			char ch = str.charAt(i);
			if (ch != ']') continue;
			Color namedColor = Colors.get(str.subSequence(colorStart, i).toString());
			if (namedColor == null) return -1; // Unknown color name.
			Color color = colorPool.obtain();
			colorStack.add(color);
			color.set(namedColor);
			return i - start;
		}
		return -1; // Unclosed color tag.
	}

	public float getModkerning () {
		return modkerning;
	}

	public void setModkerning (float modkerning) {
		this.modkerning = modkerning;
	}

	public void setModLineHeight(float modLineHeight) {
		this.modLineHeight = modLineHeight;
	}
}
