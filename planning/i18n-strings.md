# Wadjet Android — i18n String Mapping

> Maps web app i18n keys (en.json / ar.json) to Android strings.xml.
> Arabic strings go in `values-ar/strings.xml`.

---

## String Resources

### App
```xml
<!-- values/strings.xml -->
<string name="app_name">Wadjet</string>
<string name="app_tagline">Decode the Secrets of Ancient Egypt</string>
<string name="app_site_desc">AI-powered Egyptian heritage platform</string>

<!-- values-ar/strings.xml -->
<string name="app_name">وادجت</string>
<string name="app_tagline">اكتشف أسرار مصر القديمة</string>
<string name="app_site_desc">منصة التراث المصري المدعومة بالذكاء الاصطناعي</string>
```

### Common
```xml
<!-- EN -->
<string name="loading">Loading…</string>
<string name="error_generic">Something went wrong</string>
<string name="error_network">No internet connection</string>
<string name="error_timeout">Request timed out</string>
<string name="retry">Try Again</string>
<string name="back">Back</string>
<string name="next">Next</string>
<string name="previous">Previous</string>
<string name="cancel">Cancel</string>
<string name="close">Close</string>
<string name="copy">Copy</string>
<string name="share">Share</string>
<string name="search">Search</string>
<string name="save">Save</string>
<string name="delete">Delete</string>
<string name="play">Play</string>
<string name="pause">Pause</string>
<string name="stop">Stop</string>
<string name="play_narration">Play Narration</string>
<string name="copied_clipboard">Copied to clipboard</string>

<!-- AR -->
<string name="loading">جارٍ التحميل…</string>
<string name="error_generic">حدث خطأ ما</string>
<string name="error_network">لا يوجد اتصال بالإنترنت</string>
<string name="error_timeout">انتهت مهلة الطلب</string>
<string name="retry">حاول مرة أخرى</string>
<string name="back">رجوع</string>
<string name="next">التالي</string>
<string name="previous">السابق</string>
<string name="cancel">إلغاء</string>
<string name="close">إغلاق</string>
<string name="copy">نسخ</string>
<string name="share">مشاركة</string>
<string name="search">بحث</string>
<string name="save">حفظ</string>
<string name="delete">حذف</string>
<string name="play">تشغيل</string>
<string name="pause">إيقاف مؤقت</string>
<string name="stop">إيقاف</string>
<string name="play_narration">تشغيل السرد</string>
<string name="copied_clipboard">تم النسخ</string>
```

### Navigation
```xml
<!-- EN -->
<string name="nav_home">Home</string>
<string name="nav_scan">Scan</string>
<string name="nav_explore">Explore</string>
<string name="nav_stories">Stories</string>
<string name="nav_profile">Profile</string>
<string name="nav_dictionary">Dictionary</string>
<string name="nav_chat">Thoth</string>
<string name="nav_dashboard">Dashboard</string>
<string name="nav_settings">Settings</string>
<string name="nav_feedback">Feedback</string>

<!-- AR -->
<string name="nav_home">الرئيسية</string>
<string name="nav_scan">مسح</string>
<string name="nav_explore">استكشاف</string>
<string name="nav_stories">قصص</string>
<string name="nav_profile">الملف الشخصي</string>
<string name="nav_dictionary">القاموس</string>
<string name="nav_chat">تحوت</string>
<string name="nav_dashboard">لوحة التحكم</string>
<string name="nav_settings">الإعدادات</string>
<string name="nav_feedback">ملاحظات</string>
```

### Auth
```xml
<!-- EN -->
<string name="auth_welcome_back">Welcome Back</string>
<string name="auth_create_account">Create Account</string>
<string name="auth_email">Email</string>
<string name="auth_password">Password</string>
<string name="auth_display_name">Display Name</string>
<string name="auth_sign_in">Sign In</string>
<string name="auth_sign_up">Sign Up</string>
<string name="auth_sign_out">Sign Out</string>
<string name="auth_google_sign_in">Sign in with Google</string>
<string name="auth_forgot_password">Forgot Password?</string>
<string name="auth_reset_password">Reset Password</string>
<string name="auth_no_account">Don\'t have an account?</string>
<string name="auth_have_account">Already have an account?</string>
<string name="auth_password_hint">8+ chars, 1 upper, 1 lower, 1 digit</string>
<string name="auth_verification_sent">Verification email sent</string>
<string name="auth_reset_sent">Password reset email sent</string>
<string name="auth_error_invalid">Invalid email or password</string>
<string name="auth_error_exists">Account already exists</string>
<string name="auth_error_locked">Too many attempts. Try again later.</string>
<string name="auth_delete_account">Delete Account</string>
<string name="auth_delete_confirm">Are you sure? This cannot be undone.</string>

<!-- AR -->
<string name="auth_welcome_back">مرحبًا بعودتك</string>
<string name="auth_create_account">إنشاء حساب</string>
<string name="auth_email">البريد الإلكتروني</string>
<string name="auth_password">كلمة المرور</string>
<string name="auth_display_name">اسم العرض</string>
<string name="auth_sign_in">تسجيل الدخول</string>
<string name="auth_sign_up">إنشاء حساب</string>
<string name="auth_sign_out">تسجيل الخروج</string>
<string name="auth_google_sign_in">تسجيل الدخول بحساب جوجل</string>
<string name="auth_forgot_password">نسيت كلمة المرور؟</string>
<string name="auth_reset_password">إعادة تعيين كلمة المرور</string>
<string name="auth_no_account">ليس لديك حساب؟</string>
<string name="auth_have_account">لديك حساب بالفعل؟</string>
<string name="auth_password_hint">8+ أحرف، حرف كبير، حرف صغير، رقم</string>
<string name="auth_verification_sent">تم إرسال رسالة التحقق</string>
<string name="auth_reset_sent">تم إرسال رسالة إعادة التعيين</string>
<string name="auth_error_invalid">بريد إلكتروني أو كلمة مرور غير صالحة</string>
<string name="auth_error_exists">الحساب موجود بالفعل</string>
<string name="auth_error_locked">محاولات كثيرة. حاول لاحقًا.</string>
<string name="auth_delete_account">حذف الحساب</string>
<string name="auth_delete_confirm">هل أنت متأكد؟ لا يمكن التراجع عن هذا.</string>
```

### Landing
```xml
<!-- EN -->
<string name="landing_welcome">Welcome back, %s</string>
<string name="landing_hieroglyphs_title">Hieroglyphs</string>
<string name="landing_hieroglyphs_desc">Decode Ancient Egypt</string>
<string name="landing_hieroglyphs_cta">Start Scanning</string>
<string name="landing_landmarks_title">Landmarks</string>
<string name="landing_landmarks_desc">Explore Sites &amp; Monuments</string>
<string name="landing_landmarks_cta">Start Exploring</string>
<string name="landing_quick_actions">Quick Actions</string>

<!-- AR -->
<string name="landing_welcome">مرحبًا بعودتك، %s</string>
<string name="landing_hieroglyphs_title">الهيروغليفية</string>
<string name="landing_hieroglyphs_desc">فك رموز مصر القديمة</string>
<string name="landing_hieroglyphs_cta">ابدأ المسح</string>
<string name="landing_landmarks_title">المعالم</string>
<string name="landing_landmarks_desc">استكشف المواقع والآثار</string>
<string name="landing_landmarks_cta">ابدأ الاستكشاف</string>
<string name="landing_quick_actions">إجراءات سريعة</string>
```

### Scan
```xml
<!-- EN -->
<string name="scan_title">Scan Hieroglyphs</string>
<string name="scan_prompt">Point camera at hieroglyphs</string>
<string name="scan_capture">Capture</string>
<string name="scan_gallery">Gallery</string>
<string name="scan_history">History</string>
<string name="scan_step_detecting">Detecting glyphs…</string>
<string name="scan_step_classifying">Classifying signs…</string>
<string name="scan_step_transliterating">Transliterating…</string>
<string name="scan_step_translating">Translating…</string>
<string name="scan_results">Results</string>
<string name="scan_detected">Detected (%d)</string>
<string name="scan_transliteration">Transliteration</string>
<string name="scan_translation">Translation</string>
<string name="scan_no_glyphs">No hieroglyphs detected</string>
<string name="scan_try_again">Try a clearer image or different angle</string>
<string name="scan_scan_again">Scan Again</string>
<string name="scan_timing">Completed in %dms</string>
<string name="scan_save">Save to History</string>
<string name="scan_saved">Scan saved!</string>

<!-- AR -->
<string name="scan_title">مسح الهيروغليفية</string>
<string name="scan_prompt">وجّه الكاميرا نحو الهيروغليفية</string>
<string name="scan_capture">التقاط</string>
<string name="scan_gallery">المعرض</string>
<string name="scan_history">السجل</string>
<string name="scan_step_detecting">جارٍ اكتشاف الرموز…</string>
<string name="scan_step_classifying">جارٍ تصنيف العلامات…</string>
<string name="scan_step_transliterating">جارٍ النقل الصوتي…</string>
<string name="scan_step_translating">جارٍ الترجمة…</string>
<string name="scan_results">النتائج</string>
<string name="scan_detected">تم اكتشاف (%d)</string>
<string name="scan_transliteration">النقل الصوتي</string>
<string name="scan_translation">الترجمة</string>
<string name="scan_no_glyphs">لم يتم اكتشاف هيروغليفية</string>
<string name="scan_try_again">جرّب صورة أوضح أو زاوية مختلفة</string>
<string name="scan_scan_again">مسح مرة أخرى</string>
<string name="scan_timing">اكتمل في %d مللي ثانية</string>
<string name="scan_save">حفظ في السجل</string>
<string name="scan_saved">تم حفظ المسح!</string>
```

### Dictionary
```xml
<!-- EN -->
<string name="dict_title">Dictionary</string>
<string name="dict_browse">Browse</string>
<string name="dict_learn">Learn</string>
<string name="dict_write">Write</string>
<string name="dict_search_hint">Search signs…</string>
<string name="dict_all_categories">All</string>
<string name="dict_uniliteral">Uniliteral</string>
<string name="dict_biliteral">Biliteral</string>
<string name="dict_triliteral">Triliteral</string>
<string name="dict_logogram">Logogram</string>
<string name="dict_determinative">Determinative</string>
<string name="dict_meaning">Meaning</string>
<string name="dict_transliteration">Transliteration</string>
<string name="dict_phonetic">Phonetic Value</string>
<string name="dict_pronunciation">How to say it</string>
<string name="dict_fun_fact">Fun Fact</string>
<string name="dict_examples">Examples</string>
<string name="dict_lesson">Lesson %d</string>
<string name="dict_lesson_1">The Alphabet</string>
<string name="dict_lesson_2">Common Words</string>
<string name="dict_lesson_3">Royal Names</string>
<string name="dict_lesson_4">Determinatives</string>
<string name="dict_lesson_5">Reading Practice</string>

<!-- AR -->
<string name="dict_title">القاموس</string>
<string name="dict_browse">تصفح</string>
<string name="dict_learn">تعلم</string>
<string name="dict_write">اكتب</string>
<string name="dict_search_hint">ابحث عن علامات…</string>
<string name="dict_all_categories">الكل</string>
<string name="dict_uniliteral">أحادي</string>
<string name="dict_biliteral">ثنائي</string>
<string name="dict_triliteral">ثلاثي</string>
<string name="dict_logogram">رمز كلمة</string>
<string name="dict_determinative">محدد</string>
<string name="dict_meaning">المعنى</string>
<string name="dict_transliteration">النقل الصوتي</string>
<string name="dict_phonetic">القيمة الصوتية</string>
<string name="dict_pronunciation">كيفية النطق</string>
<string name="dict_fun_fact">حقيقة ممتعة</string>
<string name="dict_examples">أمثلة</string>
<string name="dict_lesson">الدرس %d</string>
<string name="dict_lesson_1">الأبجدية</string>
<string name="dict_lesson_2">كلمات شائعة</string>
<string name="dict_lesson_3">أسماء ملكية</string>
<string name="dict_lesson_4">المحددات</string>
<string name="dict_lesson_5">تدريب على القراءة</string>
```

### Write
```xml
<!-- EN -->
<string name="write_title">Write Hieroglyphs</string>
<string name="write_input_hint">Type English text…</string>
<string name="write_mode_alpha">Alpha</string>
<string name="write_mode_smart">Smart</string>
<string name="write_mode_mdc">MdC</string>
<string name="write_palette">Glyph Palette</string>
<string name="write_result">Hieroglyphs</string>
<string name="write_breakdown">Glyph Breakdown</string>

<!-- AR -->
<string name="write_title">اكتب بالهيروغليفية</string>
<string name="write_input_hint">اكتب نص بالإنجليزية…</string>
<string name="write_mode_alpha">أبجدي</string>
<string name="write_mode_smart">ذكي</string>
<string name="write_mode_mdc">MdC</string>
<string name="write_palette">لوحة الرموز</string>
<string name="write_result">الهيروغليفية</string>
<string name="write_breakdown">تفصيل الرموز</string>
```

### Explore
```xml
<!-- EN -->
<string name="explore_title">Explore Egypt</string>
<string name="explore_search_hint">Search landmarks…</string>
<string name="explore_identify">Identify</string>
<string name="explore_map">Map</string>
<string name="explore_directions">Get Directions</string>
<string name="explore_chat_about">Chat about this</string>
<string name="explore_overview">Overview</string>
<string name="explore_history">History</string>
<string name="explore_tips">Tips</string>
<string name="explore_gallery">Gallery</string>
<string name="explore_recommendations">You might also like</string>
<string name="explore_category_all">All</string>
<string name="explore_category_pharaonic">Pharaonic</string>
<string name="explore_category_islamic">Islamic</string>
<string name="explore_category_museum">Museum</string>
<string name="explore_category_coptic">Coptic</string>
<string name="explore_category_greco">Greco-Roman</string>
<string name="explore_category_natural">Natural</string>
<string name="explore_category_modern">Modern</string>

<!-- AR -->
<string name="explore_title">استكشف مصر</string>
<string name="explore_search_hint">ابحث عن معالم…</string>
<string name="explore_identify">تعرّف</string>
<string name="explore_map">خريطة</string>
<string name="explore_directions">الاتجاهات</string>
<string name="explore_chat_about">تحدث عن هذا</string>
<string name="explore_overview">نظرة عامة</string>
<string name="explore_history">التاريخ</string>
<string name="explore_tips">نصائح</string>
<string name="explore_gallery">المعرض</string>
<string name="explore_recommendations">قد يعجبك أيضًا</string>
<string name="explore_category_all">الكل</string>
<string name="explore_category_pharaonic">فرعوني</string>
<string name="explore_category_islamic">إسلامي</string>
<string name="explore_category_museum">متحف</string>
<string name="explore_category_coptic">قبطي</string>
<string name="explore_category_greco">يوناني-روماني</string>
<string name="explore_category_natural">طبيعي</string>
<string name="explore_category_modern">حديث</string>
```

### Chat
```xml
<!-- EN -->
<string name="chat_title">Thoth</string>
<string name="chat_input_hint">Ask Thoth anything…</string>
<string name="chat_send">Send</string>
<string name="chat_clear">Clear Chat</string>
<string name="chat_clear_confirm">Clear all messages?</string>
<string name="chat_greeting">I am Thoth, keeper of wisdom and scribe of the gods. What would you like to know about Ancient Egypt?</string>
<string name="chat_voice_listening">Listening…</string>
<string name="chat_limit_warning">%d messages remaining today</string>

<!-- AR -->
<string name="chat_title">تحوت</string>
<string name="chat_input_hint">اسأل تحوت أي شيء…</string>
<string name="chat_send">إرسال</string>
<string name="chat_clear">مسح المحادثة</string>
<string name="chat_clear_confirm">مسح جميع الرسائل؟</string>
<string name="chat_greeting">أنا تحوت، حارس الحكمة وكاتب الآلهة. ماذا تريد أن تعرف عن مصر القديمة؟</string>
<string name="chat_voice_listening">جارٍ الاستماع…</string>
<string name="chat_limit_warning">%d رسائل متبقية اليوم</string>
```

### Stories
```xml
<!-- EN -->
<string name="stories_title">Stories</string>
<string name="stories_beginner">Beginner</string>
<string name="stories_intermediate">Intermediate</string>
<string name="stories_advanced">Advanced</string>
<string name="stories_chapters">%d chapters</string>
<string name="stories_minutes">%d min</string>
<string name="stories_score">Score: %d</string>
<string name="stories_glyphs_learned">Glyphs learned: %d</string>
<string name="stories_correct">Correct!</string>
<string name="stories_incorrect">Not quite…</string>
<string name="stories_locked">Premium Story</string>
<string name="stories_continue">Continue Reading</string>
<string name="stories_completed">Completed!</string>

<!-- AR -->
<string name="stories_title">قصص</string>
<string name="stories_beginner">مبتدئ</string>
<string name="stories_intermediate">متوسط</string>
<string name="stories_advanced">متقدم</string>
<string name="stories_chapters">%d فصول</string>
<string name="stories_minutes">%d دقيقة</string>
<string name="stories_score">النتيجة: %d</string>
<string name="stories_glyphs_learned">رموز تعلمتها: %d</string>
<string name="stories_correct">صحيح!</string>
<string name="stories_incorrect">ليس تمامًا…</string>
<string name="stories_locked">قصة مميزة</string>
<string name="stories_continue">تابع القراءة</string>
<string name="stories_completed">مكتمل!</string>
```

### Dashboard
```xml
<!-- EN -->
<string name="dashboard_title">Dashboard</string>
<string name="dashboard_scans_today">Scans Today</string>
<string name="dashboard_total_scans">Total Scans</string>
<string name="dashboard_stories_done">Stories Done</string>
<string name="dashboard_glyphs_learned">Glyphs Learned</string>
<string name="dashboard_recent_scans">Recent Scans</string>
<string name="dashboard_favorites">Favorites</string>
<string name="dashboard_story_progress">Story Progress</string>
<string name="dashboard_landmarks">Landmarks</string>
<string name="dashboard_glyphs">Glyphs</string>
<string name="dashboard_member_since">Member since %s</string>
<string name="dashboard_no_scans">No scans yet</string>
<string name="dashboard_no_favorites">No favorites yet</string>

<!-- AR -->
<string name="dashboard_title">لوحة التحكم</string>
<string name="dashboard_scans_today">مسح اليوم</string>
<string name="dashboard_total_scans">إجمالي المسح</string>
<string name="dashboard_stories_done">قصص مكتملة</string>
<string name="dashboard_glyphs_learned">رموز تعلمتها</string>
<string name="dashboard_recent_scans">المسح الأخير</string>
<string name="dashboard_favorites">المفضلة</string>
<string name="dashboard_story_progress">تقدم القصص</string>
<string name="dashboard_landmarks">معالم</string>
<string name="dashboard_glyphs">رموز</string>
<string name="dashboard_member_since">عضو منذ %s</string>
<string name="dashboard_no_scans">لا يوجد مسح بعد</string>
<string name="dashboard_no_favorites">لا توجد مفضلات بعد</string>
```

### Settings
```xml
<!-- EN -->
<string name="settings_title">Settings</string>
<string name="settings_profile">Profile</string>
<string name="settings_language">Language</string>
<string name="settings_language_en">English</string>
<string name="settings_language_ar">العربية</string>
<string name="settings_password">Password</string>
<string name="settings_current_password">Current Password</string>
<string name="settings_new_password">New Password</string>
<string name="settings_change_password">Change Password</string>
<string name="settings_tts">Text-to-Speech</string>
<string name="settings_tts_enabled">Enabled</string>
<string name="settings_tts_speed">Speed</string>
<string name="settings_storage">Storage</string>
<string name="settings_cached_data">Cached data: %s</string>
<string name="settings_clear_cache">Clear Cache</string>
<string name="settings_about">About</string>
<string name="settings_version">Version %s</string>
<string name="settings_attribution">Built by Mr Robot</string>
<string name="settings_send_feedback">Send Feedback</string>

<!-- AR -->
<string name="settings_title">الإعدادات</string>
<string name="settings_profile">الملف الشخصي</string>
<string name="settings_language">اللغة</string>
<string name="settings_language_en">English</string>
<string name="settings_language_ar">العربية</string>
<string name="settings_password">كلمة المرور</string>
<string name="settings_current_password">كلمة المرور الحالية</string>
<string name="settings_new_password">كلمة المرور الجديدة</string>
<string name="settings_change_password">تغيير كلمة المرور</string>
<string name="settings_tts">تحويل النص إلى كلام</string>
<string name="settings_tts_enabled">مفعل</string>
<string name="settings_tts_speed">السرعة</string>
<string name="settings_storage">التخزين</string>
<string name="settings_cached_data">البيانات المخزنة: %s</string>
<string name="settings_clear_cache">مسح التخزين المؤقت</string>
<string name="settings_about">حول</string>
<string name="settings_version">الإصدار %s</string>
<string name="settings_attribution">صنع بواسطة Mr Robot</string>
<string name="settings_send_feedback">إرسال ملاحظات</string>
```

### Feedback
```xml
<!-- EN -->
<string name="feedback_title">Send Feedback</string>
<string name="feedback_category">Category</string>
<string name="feedback_bug">Bug</string>
<string name="feedback_suggestion">Suggestion</string>
<string name="feedback_praise">Praise</string>
<string name="feedback_other">Other</string>
<string name="feedback_message">Message</string>
<string name="feedback_message_hint">Describe your feedback…</string>
<string name="feedback_name">Name (optional)</string>
<string name="feedback_email">Email (optional)</string>
<string name="feedback_submit">Submit Feedback</string>
<string name="feedback_success">Thank you for your feedback!</string>
<string name="feedback_char_count">%d/1000</string>

<!-- AR -->
<string name="feedback_title">إرسال ملاحظات</string>
<string name="feedback_category">الفئة</string>
<string name="feedback_bug">خطأ</string>
<string name="feedback_suggestion">اقتراح</string>
<string name="feedback_praise">إشادة</string>
<string name="feedback_other">أخرى</string>
<string name="feedback_message">الرسالة</string>
<string name="feedback_message_hint">صف ملاحظاتك…</string>
<string name="feedback_name">الاسم (اختياري)</string>
<string name="feedback_email">البريد الإلكتروني (اختياري)</string>
<string name="feedback_submit">إرسال</string>
<string name="feedback_success">شكرًا على ملاحظاتك!</string>
<string name="feedback_char_count">%d/1000</string>
```

### Permissions
```xml
<!-- EN -->
<string name="permission_camera_title">Camera Access</string>
<string name="permission_camera_rationale">Wadjet needs camera access to scan hieroglyphs and identify landmarks.</string>
<string name="permission_microphone_title">Microphone Access</string>
<string name="permission_microphone_rationale">Wadjet needs microphone access for voice input with Thoth.</string>
<string name="permission_settings">Open Settings</string>

<!-- AR -->
<string name="permission_camera_title">الوصول إلى الكاميرا</string>
<string name="permission_camera_rationale">يحتاج وادجت إلى الكاميرا لمسح الهيروغليفية والتعرف على المعالم.</string>
<string name="permission_microphone_title">الوصول إلى الميكروفون</string>
<string name="permission_microphone_rationale">يحتاج وادجت إلى الميكروفون للإدخال الصوتي مع تحوت.</string>
<string name="permission_settings">فتح الإعدادات</string>
```

### Identify Landmark
```xml
<!-- EN -->
<string name="identify_title">Identify Landmark</string>
<string name="identify_analyzing">Identifying landmark…</string>
<string name="identify_top_matches">Top Matches</string>
<string name="identify_best_match">Best Match</string>
<string name="identify_confidence">%d%% confidence</string>
<string name="identify_no_match">No landmark detected</string>
<string name="identify_try_again">Try a different angle or clearer image</string>
<string name="identify_view_details">View Details</string>

<!-- AR -->
<string name="identify_title">التعرف على المعلم</string>
<string name="identify_analyzing">جارٍ التعرف على المعلم…</string>
<string name="identify_top_matches">أفضل التطابقات</string>
<string name="identify_best_match">أفضل تطابق</string>
<string name="identify_confidence">%d%% ثقة</string>
<string name="identify_no_match">لم يتم التعرف على معلم</string>
<string name="identify_try_again">جرّب زاوية مختلفة أو صورة أوضح</string>
<string name="identify_view_details">عرض التفاصيل</string>
```

### Notifications (FCM)
```xml
<!-- EN -->
<string name="notif_channel_story">Story Reminders</string>
<string name="notif_channel_glyph">Daily Glyph</string>
<string name="notif_channel_content">New Content</string>
<string name="notif_story_title">Continue your story</string>
<string name="notif_story_body">You left off at Chapter %d of %s</string>
<string name="notif_glyph_title">Glyph of the Day</string>
<string name="notif_glyph_body">Today\'s glyph: %s — %s</string>
<string name="notif_content_title">New in Wadjet</string>
<string name="notif_content_body">Check out what\'s new!</string>

<!-- AR -->
<string name="notif_channel_story">تذكيرات القصص</string>
<string name="notif_channel_glyph">رمز اليوم</string>
<string name="notif_channel_content">محتوى جديد</string>
<string name="notif_story_title">تابع قصتك</string>
<string name="notif_story_body">توقفت عند الفصل %d من %s</string>
<string name="notif_glyph_title">رمز اليوم</string>
<string name="notif_glyph_body">رمز اليوم: %s — %s</string>
<string name="notif_content_title">جديد في وادجت</string>
<string name="notif_content_body">اطلع على الجديد!</string>
```

### Accessibility Content Descriptions
```xml
<!-- EN -->
<string name="cd_wadjet_logo">Wadjet — Eye of Horus logo</string>
<string name="cd_camera_viewfinder">Camera viewfinder for scanning</string>
<string name="cd_detected_glyph">Detected hieroglyph: %s</string>
<string name="cd_landmark_image">Photo of %s</string>
<string name="cd_story_illustration">Story illustration for chapter %d</string>
<string name="cd_user_avatar">User profile picture</string>
<string name="cd_thoth_avatar">Thoth AI assistant</string>
<string name="cd_favorite_button">Add to favorites</string>
<string name="cd_favorite_remove">Remove from favorites</string>
<string name="cd_play_audio">Play audio narration</string>
<string name="cd_stop_audio">Stop audio narration</string>
<string name="cd_language_switch">Switch language</string>
<string name="cd_close_dialog">Close dialog</string>
<string name="cd_scan_result_image">Annotated scan result image</string>

<!-- AR -->
<string name="cd_wadjet_logo">وادجت — شعار عين حورس</string>
<string name="cd_camera_viewfinder">عدسة الكاميرا للمسح</string>
<string name="cd_detected_glyph">هيروغليفية مكتشفة: %s</string>
<string name="cd_landmark_image">صورة %s</string>
<string name="cd_story_illustration">رسم توضيحي للفصل %d</string>
<string name="cd_user_avatar">صورة الملف الشخصي</string>
<string name="cd_thoth_avatar">مساعد تحوت الذكي</string>
<string name="cd_favorite_button">إضافة للمفضلة</string>
<string name="cd_favorite_remove">إزالة من المفضلة</string>
<string name="cd_play_audio">تشغيل السرد الصوتي</string>
<string name="cd_stop_audio">إيقاف السرد الصوتي</string>
<string name="cd_language_switch">تبديل اللغة</string>
<string name="cd_close_dialog">إغلاق</string>
<string name="cd_scan_result_image">صورة نتيجة المسح</string>
```
