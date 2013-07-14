/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pahans.kichibichiya.util;

import static com.pahans.kichibichiya.util.Utils.getAllAvailableImage;
import static com.pahans.kichibichiya.util.Utils.matcherEnd;
import static com.pahans.kichibichiya.util.Utils.matcherGroup;
import static com.pahans.kichibichiya.util.Utils.matcherStart;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.pahans.kichibichiya.model.ImageSpec;
import com.twitter.Regex;

/**
 * Linkify take a piece of text and a regular expression and turns all of the
 * regex matches in the text into clickable links. This is particularly useful
 * for matching things like email addresses, web urls, etc. and making them
 * actionable.
 * 
 * Alone with the pattern that is to be matched, a url scheme prefix is also
 * required. Any pattern match that does not begin with the supplied scheme will
 * have the scheme prepended to the matched text when the clickable url is
 * created. For instance, if you are matching web urls you would supply the
 * scheme <code>http://</code>. If the pattern matches example.com, which does
 * not have a url scheme prefix, the supplied scheme will be prepended to create
 * <code>http://example.com</code> when the clickable url link is created.
 */

public class TwidereLinkify {

	public static final int LINK_TYPE_MENTION_LIST = 1;
	public static final int LINK_TYPE_HASHTAG = 2;
	public static final int LINK_TYPE_LINK_WITH_IMAGE_EXTENSION = 3;
	public static final int LINK_TYPE_LINK = 4;
	public static final int LINK_TYPE_ALL_AVAILABLE_IMAGE = 5;
	public static final int LINK_TYPE_LIST = 6;
	public static final int LINK_TYPE_CASHTAG = 7;

	public static final int[] ALL_LINK_TYPES = new int[] { LINK_TYPE_LINK, LINK_TYPE_MENTION_LIST, LINK_TYPE_HASHTAG,
			LINK_TYPE_LINK_WITH_IMAGE_EXTENSION, LINK_TYPE_ALL_AVAILABLE_IMAGE, LINK_TYPE_CASHTAG

	};

	public static final String SINA_WEIBO_IMAGES_AVAILABLE_SIZES = "(woriginal|large|thumbnail|bmiddle|mw600)";

	public static final String AVAILABLE_URL_SCHEME_PREFIX = "(https?:\\/\\/)?";
	public static final String AVAILABLE_IMAGE_SHUFFIX = "(png|jpeg|jpg|gif|bmp)";

	private static final String STRING_PATTERN_TWITTER_IMAGES_DOMAIN = "(p|pbs)\\.twimg\\.com";
	private static final String STRING_PATTERN_SINA_WEIBO_IMAGES_DOMAIN = "[\\w\\d]+\\.sinaimg\\.cn|[\\w\\d]+\\.sina\\.cn";
	private static final String STRING_PATTERN_LOCKERZ_AND_PLIXI_DOMAIN = "plixi\\.com\\/p|lockerz\\.com\\/s";
	private static final String STRING_PATTERN_INSTAGRAM_DOMAIN = "instagr\\.am|instagram\\.com";
	private static final String STRING_PATTERN_TWITPIC_DOMAIN = "twitpic\\.com";
	private static final String STRING_PATTERN_IMGLY_DOMAIN = "img\\.ly";
	private static final String STRING_PATTERN_YFROG_DOMAIN = "yfrog\\.com";
	private static final String STRING_PATTERN_TWITGOO_DOMAIN = "twitgoo\\.com";
	private static final String STRING_PATTERN_MOBYPICTURE_DOMAIN = "moby\\.to";
	private static final String STRING_PATTERN_IMGUR_DOMAIN = "imgur\\.com|i\\.imgur\\.com";
	private static final String STRING_PATTERN_PHOTOZOU_DOMAIN = "photozou\\.jp";

	private static final String STRING_PATTERN_IMAGES_NO_SCHEME = "[^:\\/\\/].+?\\." + AVAILABLE_IMAGE_SHUFFIX;
	private static final String STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME = STRING_PATTERN_TWITTER_IMAGES_DOMAIN
			+ "(\\/media)?\\/([\\d\\w\\-_]+)\\.(png|jpeg|jpg|gif|bmp)";
	private static final String STRING_PATTERN_SINA_WEIBO_IMAGES_NO_SCHEME = "("
			+ STRING_PATTERN_SINA_WEIBO_IMAGES_DOMAIN + ")" + "\\/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES
			+ "\\/(([\\d\\w]+)\\.(png|jpeg|jpg|gif|bmp))";
	private static final String STRING_PATTERN_LOCKERZ_AND_PLIXI_NO_SCHEME = "("
			+ STRING_PATTERN_LOCKERZ_AND_PLIXI_DOMAIN + ")" + "\\/(\\w+)\\/?";
	private static final String STRING_PATTERN_INSTAGRAM_NO_SCHEME = "(" + STRING_PATTERN_INSTAGRAM_DOMAIN + ")"
			+ "\\/p\\/([_\\-\\d\\w]+)\\/?";
	private static final String STRING_PATTERN_TWITPIC_NO_SCHEME = STRING_PATTERN_TWITPIC_DOMAIN + "\\/([\\d\\w]+)\\/?";
	private static final String STRING_PATTERN_IMGLY_NO_SCHEME = STRING_PATTERN_IMGLY_DOMAIN + "\\/([\\w\\d]+)\\/?";
	private static final String STRING_PATTERN_YFROG_NO_SCHEME = STRING_PATTERN_YFROG_DOMAIN + "\\/([\\w\\d]+)\\/?";
	private static final String STRING_PATTERN_TWITGOO_NO_SCHEME = STRING_PATTERN_TWITGOO_DOMAIN + "\\/([\\d\\w]+)\\/?";
	private static final String STRING_PATTERN_MOBYPICTURE_NO_SCHEME = STRING_PATTERN_MOBYPICTURE_DOMAIN
			+ "\\/([\\d\\w]+)\\/?";
	private static final String STRING_PATTERN_IMGUR_NO_SCHEME = "(" + STRING_PATTERN_IMGUR_DOMAIN + ")"
			+ "\\/([\\d\\w]+)((?-i)s|(?-i)l)?(\\." + AVAILABLE_IMAGE_SHUFFIX + ")?";
	private static final String STRING_PATTERN_PHOTOZOU_NO_SCHEME = STRING_PATTERN_PHOTOZOU_DOMAIN
			+ "\\/photo\\/show\\/([\\d]+)\\/([\\d]+)\\/?";

	private static final String STRING_PATTERN_IMAGES = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_IMAGES_NO_SCHEME;
	private static final String STRING_PATTERN_TWITTER_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME;
	private static final String STRING_PATTERN_SINA_WEIBO_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_SINA_WEIBO_IMAGES_NO_SCHEME;
	private static final String STRING_PATTERN_LOCKERZ_AND_PLIXI = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_LOCKERZ_AND_PLIXI_NO_SCHEME;
	private static final String STRING_PATTERN_INSTAGRAM = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_INSTAGRAM_NO_SCHEME;
	private static final String STRING_PATTERN_TWITPIC = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_TWITPIC_NO_SCHEME;
	private static final String STRING_PATTERN_IMGLY = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_IMGLY_NO_SCHEME;
	private static final String STRING_PATTERN_YFROG = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_YFROG_NO_SCHEME;
	private static final String STRING_PATTERN_TWITGOO = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_TWITGOO_NO_SCHEME;
	private static final String STRING_PATTERN_MOBYPICTURE = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_MOBYPICTURE_NO_SCHEME;
	private static final String STRING_PATTERN_IMGUR = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_IMGUR_NO_SCHEME;
	private static final String STRING_PATTERN_PHOTOZOU = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_PHOTOZOU_NO_SCHEME;

	public static final Pattern PATTERN_ALL_AVAILABLE_IMAGES = Pattern.compile(AVAILABLE_URL_SCHEME_PREFIX + "("
			+ STRING_PATTERN_IMAGES_NO_SCHEME + "|" + STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME + "|"
			+ STRING_PATTERN_SINA_WEIBO_IMAGES_NO_SCHEME + "|" + STRING_PATTERN_LOCKERZ_AND_PLIXI_NO_SCHEME + "|"
			+ STRING_PATTERN_INSTAGRAM_NO_SCHEME + "|" + STRING_PATTERN_TWITPIC_NO_SCHEME + "|"
			+ STRING_PATTERN_IMGLY_NO_SCHEME + "|" + STRING_PATTERN_YFROG_NO_SCHEME + "|"
			+ STRING_PATTERN_TWITGOO_NO_SCHEME + "|" + STRING_PATTERN_MOBYPICTURE_NO_SCHEME + "|"
			+ STRING_PATTERN_IMGUR_NO_SCHEME + "|" + STRING_PATTERN_PHOTOZOU_NO_SCHEME + ")", Pattern.CASE_INSENSITIVE);

	public static final Pattern PATTERN_INLINE_PREVIEW_AVAILABLE_IMAGES_MATCH_ONLY = Pattern.compile(
			AVAILABLE_URL_SCHEME_PREFIX + "(" + STRING_PATTERN_IMAGES_NO_SCHEME + "|"
					+ STRING_PATTERN_TWITTER_IMAGES_DOMAIN + "|" + STRING_PATTERN_SINA_WEIBO_IMAGES_DOMAIN + "|"
					+ STRING_PATTERN_LOCKERZ_AND_PLIXI_DOMAIN + "|" + STRING_PATTERN_INSTAGRAM_DOMAIN + "|"
					+ STRING_PATTERN_TWITPIC_DOMAIN + "|" + STRING_PATTERN_IMGLY_DOMAIN + "|"
					+ STRING_PATTERN_YFROG_DOMAIN + "|" + STRING_PATTERN_TWITGOO_DOMAIN + "|"
					+ STRING_PATTERN_MOBYPICTURE_DOMAIN + "|" + STRING_PATTERN_IMGUR_DOMAIN + "|"
					+ STRING_PATTERN_PHOTOZOU_DOMAIN + ")", Pattern.CASE_INSENSITIVE);

	public static final Pattern PATTERN_PREVIEW_AVAILABLE_IMAGES_IN_HTML = Pattern.compile("<a href=\"("
			+ AVAILABLE_URL_SCHEME_PREFIX + "(" + STRING_PATTERN_IMAGES_NO_SCHEME + "|"
			+ STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME + "|" + STRING_PATTERN_SINA_WEIBO_IMAGES_NO_SCHEME + "|"
			+ STRING_PATTERN_LOCKERZ_AND_PLIXI_NO_SCHEME + "|" + STRING_PATTERN_INSTAGRAM_NO_SCHEME + "|"
			+ STRING_PATTERN_TWITPIC_NO_SCHEME + "|" + STRING_PATTERN_IMGLY_NO_SCHEME + "|"
			+ STRING_PATTERN_YFROG_NO_SCHEME + "|" + STRING_PATTERN_TWITGOO_NO_SCHEME + "|"
			+ STRING_PATTERN_MOBYPICTURE_NO_SCHEME + "|" + STRING_PATTERN_IMGUR_NO_SCHEME + "|"
			+ STRING_PATTERN_PHOTOZOU_NO_SCHEME + "))\">", Pattern.CASE_INSENSITIVE);

	public static final int PREVIEW_AVAILABLE_IMAGES_IN_HTML_GROUP_LINK = 1;

	public static final Pattern PATTERN_IMAGES = Pattern.compile(STRING_PATTERN_IMAGES, Pattern.CASE_INSENSITIVE);
	public static final Pattern PATTERN_TWITTER_IMAGES = Pattern.compile(STRING_PATTERN_TWITTER_IMAGES,
			Pattern.CASE_INSENSITIVE);
	public static final Pattern PATTERN_SINA_WEIBO_IMAGES = Pattern.compile(STRING_PATTERN_SINA_WEIBO_IMAGES,
			Pattern.CASE_INSENSITIVE);
	public static final Pattern PATTERN_LOCKERZ_AND_PLIXI = Pattern.compile(STRING_PATTERN_LOCKERZ_AND_PLIXI,
			Pattern.CASE_INSENSITIVE);

	public static final Pattern PATTERN_INSTAGRAM = Pattern.compile(STRING_PATTERN_INSTAGRAM, Pattern.CASE_INSENSITIVE);
	public static final int INSTAGRAM_GROUP_ID = 3;

	public static final Pattern PATTERN_TWITPIC = Pattern.compile(STRING_PATTERN_TWITPIC, Pattern.CASE_INSENSITIVE);
	public static final int TWITPIC_GROUP_ID = 2;

	public static final Pattern PATTERN_IMGLY = Pattern.compile(STRING_PATTERN_IMGLY, Pattern.CASE_INSENSITIVE);
	public static final int IMGLY_GROUP_ID = 2;

	public static final Pattern PATTERN_YFROG = Pattern.compile(STRING_PATTERN_YFROG, Pattern.CASE_INSENSITIVE);
	public static final int YFROG_GROUP_ID = 2;

	public static final Pattern PATTERN_TWITGOO = Pattern.compile(STRING_PATTERN_TWITGOO, Pattern.CASE_INSENSITIVE);
	public static final int TWITGOO_GROUP_ID = 2;

	public static final Pattern PATTERN_MOBYPICTURE = Pattern.compile(STRING_PATTERN_MOBYPICTURE,
			Pattern.CASE_INSENSITIVE);
	public static final int MOBYPICTURE_GROUP_ID = 2;

	public static final Pattern PATTERN_IMGUR = Pattern.compile(STRING_PATTERN_IMGUR, Pattern.CASE_INSENSITIVE);
	public static final int IMGUR_GROUP_ID = 3;

	public static final Pattern PATTERN_PHOTOZOU = Pattern.compile(STRING_PATTERN_PHOTOZOU, Pattern.CASE_INSENSITIVE);
	public static final int PHOTOZOU_GROUP_ID = 3;

	public static final String TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES = "(bigger|normal|mini)";
	private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME = "([\\w\\d]+)\\.twimg\\.com\\/profile_images\\/([\\d\\w\\-_]+)\\/([\\d\\w\\-_]+)_"
			+ TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES + "(\\.?" + AVAILABLE_IMAGE_SHUFFIX + ")?";
	private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME;

	public static final Pattern PATTERN_TWITTER_PROFILE_IMAGES = Pattern.compile(STRING_PATTERN_TWITTER_PROFILE_IMAGES,
			Pattern.CASE_INSENSITIVE);

	private final TextView view;

	private OnLinkClickListener mOnLinkClickListener;

	public TwidereLinkify(final TextView view) {
		this.view = view;
		view.setMovementMethod(LinkMovementMethod.getInstance());
	}

	public final void addAllLinks() {
		for (final int type : ALL_LINK_TYPES) {
			addLinks(type);
		}
	}

	/**
	 * Applies a regex to the text of a TextView turning the matches into links.
	 * If links are found then UrlSpans are applied to the link text match
	 * areas, and the movement method for the text is changed to
	 * LinkMovementMethod.
	 * 
	 * @param description TextView whose text is to be marked-up with links
	 * @param pattern Regex pattern to be used for finding links
	 * @param scheme Url scheme string (eg <code>http://</code> to be prepended
	 *            to the url of links that do not have a scheme specified in the
	 *            link text
	 */
	public final void addLinks(final int type) {
		final SpannableString string = SpannableString.valueOf(view.getText());
		switch (type) {
			case LINK_TYPE_MENTION_LIST: {
				addMentionOrListLinks(string);
				break;
			}
			case LINK_TYPE_HASHTAG: {
				addHashtagLinks(string);
				break;
			}
			case LINK_TYPE_LINK_WITH_IMAGE_EXTENSION: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final int start = string.getSpanStart(span);
					final int end = string.getSpanEnd(span);
					final String url = span.getURL();
					if (PATTERN_IMAGES.matcher(url).matches()) {
						string.removeSpan(span);
						applyLink(url, start, end, string, LINK_TYPE_LINK_WITH_IMAGE_EXTENSION);
					}
				}
				break;
			}
			case LINK_TYPE_LINK: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final int start = string.getSpanStart(span);
					final int end = string.getSpanEnd(span);
					if (start < 0 || end > string.length() || start > end) {
						continue;
					}
					string.removeSpan(span);
					applyLink(span.getURL(), start, end, string, LINK_TYPE_LINK);
				}
				break;
			}
			case LINK_TYPE_ALL_AVAILABLE_IMAGE: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final Matcher matcher = PATTERN_ALL_AVAILABLE_IMAGES.matcher(span.getURL());
					if (matcher.matches()) {
						final ImageSpec spec = getAllAvailableImage(matcher.group());
						final int start = string.getSpanStart(span);
						final int end = string.getSpanEnd(span);
						if (spec == null || start < 0 || end > string.length() || start > end) {
							continue;
						}
						final String url = spec.image_link;
						string.removeSpan(span);
						applyLink(url, start, end, string, LINK_TYPE_LINK_WITH_IMAGE_EXTENSION);
					}
				}
				break;
			}
			case LINK_TYPE_CASHTAG: {
				addCashtagLinks(string);
				break;
			}
			default: {
				return;
			}

		}

		view.setText(string);
		addLinkMovementMethod(view);
	}

	public OnLinkClickListener getmOnLinkClickListener() {
		return mOnLinkClickListener;
	}

	public void setOnLinkClickListener(final OnLinkClickListener listener) {
		mOnLinkClickListener = listener;
	}

	private final boolean addCashtagLinks(final Spannable spannable) {
		boolean hasMatches = false;
		final Matcher matcher = Regex.VALID_CASHTAG.matcher(spannable);

		while (matcher.find()) {
			final int start = matcherStart(matcher, Regex.VALID_CASHTAG_GROUP_CASHTAG_FULL);
			final int end = matcherEnd(matcher, Regex.VALID_CASHTAG_GROUP_CASHTAG_FULL);
			final String url = matcherGroup(matcher, Regex.VALID_CASHTAG_GROUP_TAG);

			applyLink(url, start, end, spannable, LINK_TYPE_HASHTAG);
			hasMatches = true;
		}

		return hasMatches;
	}

	private final boolean addHashtagLinks(final Spannable spannable) {
		boolean hasMatches = false;
		final Matcher matcher = Regex.VALID_HASHTAG.matcher(spannable);

		while (matcher.find()) {
			final int start = matcherStart(matcher, Regex.VALID_HASHTAG_GROUP_HASHTAG_FULL);
			final int end = matcherEnd(matcher, Regex.VALID_HASHTAG_GROUP_HASHTAG_FULL);
			final String url = matcherGroup(matcher, Regex.VALID_HASHTAG_GROUP_HASHTAG_FULL);

			applyLink(url, start, end, spannable, LINK_TYPE_HASHTAG);
			hasMatches = true;
		}

		return hasMatches;
	}

	private final boolean addMentionOrListLinks(final Spannable spannable) {
		boolean hasMatches = false;
		final Matcher matcher = Regex.VALID_MENTION_OR_LIST.matcher(spannable);

		while (matcher.find()) {
			final int start = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_AT);
			final int username_end = matcherEnd(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
			final int list_start = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			final int list_end = matcherEnd(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			final String mention = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
			final String list = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			applyLink(mention, start, username_end, spannable, LINK_TYPE_MENTION_LIST);
			if (list_start >= 0 && list_end >= 0) {
				applyLink(mention + "/" + list, list_start, list_end, spannable, LINK_TYPE_LIST);
			}
			hasMatches = true;
		}

		return hasMatches;
	}

	private final void applyLink(final String url, final int start, final int end, final Spannable text, final int type) {
		final LinkSpan span = new LinkSpan(url, type, mOnLinkClickListener);

		text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	private static final void addLinkMovementMethod(final TextView t) {
		final MovementMethod m = t.getMovementMethod();

		if (m == null || !(m instanceof LinkMovementMethod)) {
			if (t.getLinksClickable()) {
				t.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
	}

	public interface OnLinkClickListener {
		public void onLinkClick(String link, int type);
	}

	static class LinkSpan extends URLSpan {

		private final int type;
		private final OnLinkClickListener listener;

		public LinkSpan(final String url, final int type, final OnLinkClickListener listener) {
			super(url);
			this.type = type;
			this.listener = listener;
		}

		@Override
		public void onClick(final View widget) {
			if (listener != null) {
				listener.onLinkClick(getURL(), type);
			}
		}

	}
}