package ui;

import java.awt.Color;
import java.awt.Font;

/** 統一色彩與字型常數（參考截圖米白風格） */
public class AppColors {
    // 背景
    public static final Color BG_SIDEBAR  = new Color(0xE8DCC8); // 米棕側欄
    public static final Color BG_MAIN     = new Color(0xF5EFE0); // 主畫面米白
    public static final Color BG_CARD     = new Color(0xFFFDF5); // 卡片白
    public static final Color BG_SELECTED = new Color(0xA8C8C0); // 選中項目（青色）

    // 邊框
    public static final Color BORDER      = new Color(0x2D2D2D); // 深色粗框
    public static final Color BORDER_SOFT = new Color(0xC8BAA0); // 淡框

    // 文字
    public static final Color TEXT_PRIMARY   = new Color(0x1A1A1A);
    public static final Color TEXT_SECONDARY = new Color(0x5A5A5A);
    public static final Color TEXT_GREEN     = new Color(0x2E7D32);
    public static final Color TEXT_RED       = new Color(0xC62828);
    public static final Color TEXT_ORANGE    = new Color(0xE65100);

    // 按鈕
    public static final Color BTN_PRIMARY    = new Color(0x7C6250);
    public static final Color BTN_SUCCESS    = new Color(0x2E7D32);
    public static final Color BTN_DANGER     = new Color(0xC62828);
    public static final Color BTN_NEUTRAL    = new Color(0xF5EFE0);

    // 字型
    public static final Font FONT_TITLE  = new Font("Microsoft JhengHei", Font.BOLD,  22);
    public static final Font FONT_HEAD   = new Font("Microsoft JhengHei", Font.BOLD,  15);
    public static final Font FONT_BODY   = new Font("Microsoft JhengHei", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Microsoft JhengHei", Font.PLAIN, 11);
    public static final Font FONT_WORD   = new Font("Serif", Font.BOLD, 32);
    public static final Font FONT_BTN    = new Font("Microsoft JhengHei", Font.BOLD,  13);

    private AppColors() {}
}
