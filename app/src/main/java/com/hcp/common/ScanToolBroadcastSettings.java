package com.hcp.common;

/**
 * Created by tmzhiPC on 2016-10-31.
 */
public class ScanToolBroadcastSettings {
    public static final String ACTION_APP_SETTING = "com.android.scanner.service_settings";

    public static final String TYPE_PLAYSOUND = "sound_play";
    public static final String TYPE_VIBERATE = "viberate";
    public static final String TYPE_BOOT_START = "boot_start";
    public static final String TYPE_END_CHAR = "endchar";
    public static final String TYPE_BARCODE_SEND_MODE = "barcode_send_mode";
    public static final String TYPE_BARCODE_BROADCAST_ACTION = "action_barcode_broadcast";
    public static final String TYPE_BARCODE_BROADCAST_KEY = "key_barcode_broadcast";
    public static final String TYPE_SCAN_CONTINUE="scan_continue";	//连续扫描
    public static final String TYPE_INTERVAL="interval";	//连续扫面时间间隔
    public static final String TYPE_PREFIX="prefix";		//条码前缀参数
    public static final String TYPE_SUFFIX="suffix";		//条码后缀参数
    public static final String TYPE_ENDCHAR_ON_EMU = "end_char_on_emu"; //结束符以模拟按键方式发送
    public static final String TYPE_ENTER_EVENT = "end_event"; //默认广播时条码后添加回车事件
    public static final String TYPE_FILTER_INVISIBLE_CHARS = "filter_invisible_chars"; //过滤不可见字符
}
