package com.bobby.pictures.util;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

/**
 * <HR>
 * 作者: 孙博
 * <p/>
 * 时间: 2013年11月5日 上午10:15:36
 */
public final class ParseText
{
    /**
     * 为包含有数字的字符串内容重新设置为一种固定的风格。
     * <p/>
     * 被设置后的内容，普通文字风格不变，唯所有数字被整体改变为红色粗体样式，具体大小由参数决定
     *
     * @param content  要被改变风格的字符串内容
     * @param fontSize 设置被改变后的数字大小
     * @return 若参数<CODE>content</CODE>的值为{@code null}，则返回{@code null}
     * ，否则返回已改变风格后的内容
     */
    public static SpannableString setDigitalContentStyle(String content, int fontSize)
    {
        if (null == content)
            return null;
        SpannableString mSpannableString = SpannableString.valueOf(content);
        List<GroupItem> listGroupItems = analysisTextPattern(Pattern.compile("\\d+"), content);
        for (GroupItem item : listGroupItems)
        {
            int begin = item.beginIndex;
            int end = item.endIndex;
            mSpannableString.setSpan(new AbsoluteSizeSpan(fontSize, false), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannableString.setSpan(new StyleSpan(Typeface.BOLD), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannableString.setSpan(new ForegroundColorSpan(Color.RED), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return mSpannableString;
    }

    /**
     * 为包含有数字的字符串内容重新设置为一种固定的风格。
     * <p/>
     * 被设置后的内容，普通文字风格不变，唯所有数字被整体改变，粗体样式，颜色和大小由参数值决定
     *
     * @param content  要被改变风格的字符串内容
     * @param fontSize 设置被改变后的数字大小
     * @param color    被设置后的数字显示颜色
     * @return 若参数<CODE>content</CODE>的值为{@code null}，则返回{@code null}
     * ，否则返回已改变风格后的内容
     */
    public static SpannableString setDigitalContentStyle(String content, int fontSize, int color)
    {
        if (null == content)
            return null;
        SpannableString mSpannableString = SpannableString.valueOf(content);
        List<GroupItem> listGroupItems = analysisTextPattern(Pattern.compile("\\d+"), content);
        for (GroupItem item : listGroupItems)
        {
            int begin = item.beginIndex;
            int end = item.endIndex;
            mSpannableString.setSpan(new AbsoluteSizeSpan(fontSize, false), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannableString.setSpan(new StyleSpan(Typeface.BOLD), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannableString.setSpan(new ForegroundColorSpan(color), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return mSpannableString;
    }

    /**
     * 为包含有数字的字符串内容重新设置为一种固定的风格。
     * <p/>
     * 被设置后的内容，普通文字风格不变，唯所有数字被整体改变，颜色，样式和大小由参数值决定
     *
     * @param content   要被改变风格的字符串内容
     * @param fontSize  设置被改变后的数字大小
     * @param color     被设置后的数字显示颜色
     * @param fontStyle 被设置后的数字显示样式。可参考<CODE>{@link Typeface#BOLD}
     *                  </CODE>等常量定义
     * @return 若参数<CODE>content</CODE>的值为{@code null}，则返回{@code null}
     * ，否则返回已改变风格后的内容
     */
    public static SpannableString setDigitalContentStyle(String content, int fontSize, int color,
                                                         int fontStyle)
    {
        if (null == content)
            return null;
        return arrangeContentStyleForMatcher(content, "\\d+", color, fontSize, fontStyle);
    }

    /**
     * 为匹配特定正则表达式的字符串内容重新设置为一种新的风格。
     * <p/>
     * 被设置后的内容，普通文字风格不变，唯所有数字被整体改变，颜色，样式和大小由参数值决定
     *
     * @param source    要被改变风格的字符串内容
     * @param matcher   正则表达式
     * @param color     被设置后的数字显示颜色
     * @param fontSize  设置被改变后的数字大小
     * @param fontStyle 被设置后的数字显示样式。可参考<CODE>{@link Typeface#BOLD}
     *                  </CODE>等常量定义
     * @return 若参数<CODE>source</CODE>的值为{@code null}，则返回{@code null}
     * ，否则返回已改变风格后的内容
     */
    public static SpannableString arrangeContentStyleForMatcher(String source, String matcher,
                                                                int color, int fontSize, int fontStyle)
    {
        if (null == source)
            return null;
        SpannableString mSpannableString = SpannableString.valueOf(source);
        List<GroupItem> listGroupItems = analysisTextPattern(Pattern.compile(matcher), source);
        for (GroupItem item : listGroupItems)
        {
            int begin = item.beginIndex;
            int end = item.endIndex;
            mSpannableString.setSpan(new AbsoluteSizeSpan(fontSize, false), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannableString.setSpan(new StyleSpan(fontStyle), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannableString.setSpan(new ForegroundColorSpan(color), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return mSpannableString;
    }

    public static SpannableString arrangeContentStyleForPositions(String source,
                                                                  List<Integer[]> positions, int color, int fontSize,
                                                                  int fontStyle)
    {
        if (null == source)
            return null;
        SpannableString mSpannableString = SpannableString.valueOf(source);
        for (Integer[] position : positions)
        {
            int begin = position[0];
            int end = position[1];
            mSpannableString.setSpan(new AbsoluteSizeSpan(fontSize, false), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannableString.setSpan(new StyleSpan(fontStyle), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannableString.setSpan(new ForegroundColorSpan(color), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return mSpannableString;
    }

    /**
     * 为包含有begin到end之间的字符串重新设置一种自定义的风格
     *
     * @param source    要被改变风格的原字符串内容
     * @param begin     在原字符串中的某个起点位置
     * @param end       在原字符串中的某个结束点位置。<I>该参数值必须大于begin参数值</I>
     * @param color     被设置后的数字显示颜色
     * @param fontSize  设置被改变后的数字大小
     * @param fontStyle 被设置后的数字显示样式。可参考<CODE>{@link Typeface#BOLD}
     *                  </CODE>等常量定义
     * @return 若参数<CODE>source</CODE>的值为{@code null}，则返回{@code null}
     * ，否则返回已改变风格后的内容
     */
    public static SpannableString arrangeContentStyle(String source, int begin, int end, int color,
                                                      int fontSize, int fontStyle)
    {
        return arrangeContentStyle(source, new int[]{begin}, new int[]{end}, color, fontSize, fontStyle);
    }

    /**
     * 为包含有begin到end之间的字符串重新设置一种自定义的风格
     *
     * @param source    要被改变风格的原字符串内容
     * @param begin     在原字符串中的某个起点位置（若对开始位置的要求并无不同之处）
     * @param ends      在原字符串中的某个结束点位置。（多语言需要严格按照中文，英文的顺序进行）<I>该参数值必须大于begin参数值</I>
     * @param color     被设置后的数字显示颜色
     * @param fontSize  设置被改变后的数字大小
     * @param fontStyle 被设置后的数字显示样式。可参考<CODE>{@link Typeface#BOLD}
     *                  </CODE>等常量定义
     * @return 若参数<CODE>source</CODE>的值为{@code null}，则返回{@code null}
     * ，否则返回已改变风格后的内容
     */
    public static SpannableString arrangeContentStyle(String source, int begin, int[] ends, int color,
                                                      int fontSize, int fontStyle)
    {
        return arrangeContentStyle(source, new int[]{begin}, ends, color, fontSize, fontStyle);
    }

    /**
     * 为包含有begin到end之间的字符串重新设置一种自定义的风格
     *
     * @param source    要被改变风格的原字符串内容
     * @param begins    在原字符串中的某个起点位置（多语言需要严格按照中文，英文的顺序进行）
     * @param end       在原字符串中的某个结束点位置（若对结束位置的要求并无不同之处）<I>该参数值必须大于begin参数值</I>
     * @param color     被设置后的数字显示颜色
     * @param fontSize  设置被改变后的数字大小
     * @param fontStyle 被设置后的数字显示样式。可参考<CODE>{@link Typeface#BOLD}
     *                  </CODE>等常量定义
     * @return 若参数<CODE>source</CODE>的值为{@code null}，则返回{@code null}
     * ，否则返回已改变风格后的内容
     */
    public static SpannableString arrangeContentStyle(String source, int[] begins, int end, int color,
                                                      int fontSize, int fontStyle)
    {
        return arrangeContentStyle(source, begins, new int[]{end}, color, fontSize, fontStyle);
    }

    /**
     * 为包含有begin到end之间的字符串重新设置一种自定义的风格
     *
     * @param source    要被改变风格的原字符串内容
     * @param begins    在原字符串中的某个起点位置（多语言需要严格按照中文，英文的顺序进行）
     * @param ends      在原字符串中的某个结束点位置。（多语言需要严格按照中文，英文的顺序进行）<I>该参数值必须大于begin参数值</I>
     * @param color     被设置后的数字显示颜色
     * @param fontSize  设置被改变后的数字大小
     * @param fontStyle 被设置后的数字显示样式。可参考<CODE>{@link Typeface#BOLD}
     *                  </CODE>等常量定义
     * @return 若参数<CODE>source</CODE>的值为{@code null}，则返回{@code null}
     * ，否则返回已改变风格后的内容
     */
    public static SpannableString arrangeContentStyle(String source, int[] begins, int[] ends, int color,
                                                      int fontSize, int fontStyle)
    {
        if (null == source)
            return null;
        SpannableString mSpannableString = SpannableString.valueOf(source);
        int begin = begins[0];
        int end = ends[0];
        mSpannableString.setSpan(new AbsoluteSizeSpan(fontSize, false), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        mSpannableString.setSpan(new StyleSpan(fontStyle), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        mSpannableString.setSpan(new ForegroundColorSpan(color), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        return mSpannableString;
    }

    /**
     * 为包含有begin到end之间的字符串重新设置一种自定义的风格
     *
     * @param source    要被改变风格的原字符串内容
     * @param begin     在原字符串中的某个起点位置
     * @param end       在原字符串中的某个结束点位置。<I>该参数值必须大于begin参数值</I>
     * @param fontSize  设置被改变后的数字大小
     * @param fontStyle 被设置后的数字显示样式。可参考<CODE>{@link Typeface#BOLD}
     *                  </CODE>等常量定义
     * @return 若参数<CODE>source</CODE>的值为{@code null}，则返回{@code null}
     * ，否则返回已改变风格后的内容
     */
    public static SpannableString arrangeContentStyle(String source, int begin, int end,
                                                      int fontSize, int fontStyle)
    {
        if (null == source)
            return null;
        SpannableString mSpannableString = SpannableString.valueOf(source);
        mSpannableString.setSpan(new AbsoluteSizeSpan(fontSize, false), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        mSpannableString.setSpan(new StyleSpan(fontStyle), begin, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        return mSpannableString;
    }

    /**
     * 以指定的正则表达式要求的模式来分解参数content的文本内容
     *
     * @param pattern 希望被分解后的内容应该匹配的正则表达式
     * @param content 被分解的文本内容数据源
     * @return 返回一个已经封装好的内容结果集合
     */
    public static List<GroupItem> analysisTextPattern(Pattern pattern, CharSequence content)
    {
        List<GroupItem> listGroupItems = new ArrayList<>();
        Matcher matcher = pattern.matcher(content);
        while (matcher.find())
        {
            GroupItem item = new GroupItem();
            item.beginIndex = matcher.start();
            item.endIndex = matcher.end();
            item.value = matcher.group();
            listGroupItems.add(item);
        }
        return listGroupItems;
    }

    /**
     * 用以封装被分解数据之后的属性值数据模型
     * <HR>
     * 作者: 孙博
     * <p/>
     * 时间: 2013年11月6日 下午3:44:46
     */
    private final static class GroupItem implements Serializable
    {
        private static final long serialVersionUID = -5447409204214324483L;
        /**
         * 结果
         */
        String value;
        /**
         * 起始位置
         */
        int beginIndex;
        /**
         * 结束位置
         */
        int endIndex;
    }
}
