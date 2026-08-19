# FlashLearn

## مرحله 18: تکمیل Localization + بررسی Edge Case های بند 64 (جدید)

### تکمیل Localization (بند 83)
مهاجرت باقی‌مانده از مرحله ۱۷ کامل شد: هر ۱۸ فایل Composable/ViewModel باقی‌مانده
(`AddAI`, `AddHome`, `AddManual`, `AddPasteText`, `AddImportFile`, `ReviewSession`,
`ReviewTypeSelect`, `AISettings`, `BackupRestore`, `SettingsHome`, `Statistics`,
`ConceptDetail`, `VocabularyList` و ViewModel های مرتبط) به `strings.xml` مهاجرت
داده شدند. `strings.xml` حالا حدود ۱۴۰ رشته دارد.

چون ViewModel ها طبق معماری این پروژه به Context دسترسی ندارند، یک کلاس کمکی جدید
`UiText` (در `core/util/UiText.kt`) اضافه شد: ViewModel به‌جای رشته خام یک
`UiText.Resource(id, args)` (برای پیام‌های از‌پیش‌تعریف‌شده) یا `UiText.Dynamic(text)`
(برای پیام خام یک Exception که اصلاً قابل ترجمه نیست) برمی‌گرداند؛ فقط لایه Compose با
اکستنشن `@Composable fun UiText.asString()` آن را به متن نهایی تبدیل می‌کند.

در همین مسیر یک متن اشتباه هم اصلاح شد: هشدار امنیتی صفحه AI Settings می‌گفت API Key
رمزنگاری نمی‌شود، در حالی‌که از مرحله ۱۶ با `SecureKeyValueStore`/Android Keystore
رمزنگاری می‌شود.

### بررسی Edge Case های بند 64
سه Edge Case مشخص‌شده بررسی و مستند شدند؛ دو مورد نیاز به تغییر کد واقعی داشتند:

1. **تغییر Timezone** — بررسی شد، نیاز به تغییر کد نبود: `nextReviewAt` و تمام
   مقایسه‌های due-date بر اساس Epoch Millis مطلق (UTC) هستند، نه ساعت محلی؛ پس تغییر
   Timezone دستگاه هیچ تأثیری روی زمان‌بندی مرور ندارد. تنها چیزی که به Timezone وابسته
   است محدوده «امروز» در صفحه آمار است (`DateTimeUtils.startOfDay`) که **باید** با ساعت
   محلی فعلی محاسبه شود — این رفتار از قبل درست بود.
   (نکته کوچک شناخته‌شده: `addDays` یک افزایش ۲۴ساعته ثابت است نه «روز تقویمی»، پس اگر
   دقیقاً وسط بازه ۷/۳۰ روزه DST تغییر کند، زمان نمایش کارت ممکن است تا ۱ ساعت جابه‌جا
   شود — تأثیری روی صحت الگوریتم ندارد و عمداً ساده نگه داشته شده.)

2. **بستن اپ وسط Review** — بررسی شد: چون هر پاسخ بلافاصله در `answer()` با
   `LearningState`/`ReviewHistory` ذخیره می‌شود، هیچ داده‌ای در صورت Kill شدن پردازه از
   دست نمی‌رود. تنها اثر جانبی، یک ردیف `ReviewSession` با `endedAt = null` باقی‌مانده
   بود (فعلاً در جایی خوانده نمی‌شود، پس بی‌ضرر است). برای پوشش بهتر حالت خروج با دکمه
   Back/ناوبری (نه Kill کامل توسط سیستم‌عامل)، `ReviewSessionViewModel.onCleared()` حالا
   جلسه ناتمام را می‌بندد.

3. **Import ناقص** (رفع شد — این مورد واقعاً یک باگ بود):
   - `JsonBackupServiceImpl.applyImport` قبلاً در یک حلقه ساده چند رکورد به‌هم‌وابسته
     (Concept → LearningState → ReviewHistory با یک `uuidToConceptId` مشترک) را جدا‌جدا
     درج می‌کرد؛ اگر وسط راه Exception می‌افتاد (رکورد خراب، کمبود حافظه، کرش)، دیتابیس در
     وضعیتی ناسازگار (مثلاً Concept بدون LearningState) می‌ماند. حالا کل عملیات در یک
     `database.withTransaction { }` اجرا می‌شود؛ یا کامل انجام می‌شود یا کامل Rollback.
   - `ImportParsedEntriesUseCase` (مسیر Paste Text و Import File) برعکس، رکوردهایش کاملاً
     مستقل از هم هستند؛ قبلاً یک خطا در یک ردیف کل عملیات را متوقف می‌کرد (و چون هیچ
     `try/catch`ای دور آن در ViewModel نبود، می‌توانست کل اپ را Crash کند). حالا هر ردیف
     در `try/catch` جدا Import می‌شود (ردیف خراب Skip می‌شود، بقیه ادامه پیدا می‌کنند) و
     خود ViewModel ها (`AddPasteTextViewModel`, `AddImportFileViewModel`) هم یک لایه
     `try/catch` دارند تا در خطای غیرمنتظره `isImporting` برای همیشه گیر نکند و پیام خطا
     (`UiText`) به کاربر نمایش داده شود.

---

## مرحله 17: صیقل نهایی UI — Sort، Empty State، شروع Localization
- **Sort در Vocabulary List** (بند 38): دو حالت «جدیدترین» (پیش‌فرض) و «الفبایی» (بر اساس
  متن زبان مبدا) — با Query های جدید `getPageAlphabetical`/`getPageAlphabeticalInCategory`
  در `ConceptDao` که با فیلتر Category هم ترکیب می‌شوند؛ حالت الفبایی هنگام جستجوی فعال
  اعمال نمی‌شود (نتایج جستجو بر اساس تازگی می‌مانند)
- **Empty State بهتر** در Vocabulary List: پیام متفاوت برای «هنوز هیچی اضافه نشده» در برابر
  «با این فیلتر چیزی نیست»، به‌همراه یک آیکون ساده (بند 69)
- **شروع Localization واقعی** (بند 83): `res/values/strings.xml` ساخته شد و دو صفحه کامل
  (`HomeScreen`, `OnboardingScreen`) به‌طور کامل از `stringResource()` استفاده می‌کنند —
  نه فقط ساختار، بلکه یک نمونه واقعی و کامل از الگوی صحیح

### Gap شناخته‌شده (کار مکانیکی باقی‌مانده)
مهاجرت باقی ۲۵+ فایل Composable دیگر به `strings.xml` انجام نشده — این کار عمدتاً مکانیکی
است (جابه‌جایی رشته‌های فارسی موجود به Resource، بدون تغییر منطق) و در تکرار(های) بعدی
تکمیل می‌شود. ساختار و الگوی صحیح (که در Home/Onboarding دیده می‌شود) از قبل جا افتاده است.

---

## مرحله 16: تکمیل Backup/Restore (جدید)
- **رمزنگاری API Key** (رفع نکته امنیتی مرحله ۱۰): `SecureKeyValueStore` با
  `EncryptedSharedPreferences` + Android Keystore (AES256-GCM) — فقط API Key حساس اینجاست؛
  Endpoint/Model حساس نیستند و همچنان در دیتابیس معمولی می‌مانند
- **Merge Conflict Resolution کامل** (چهارمین گزینه بند 47): `ConflictResolution.MERGE` —
  فیلد‌به‌فیلد ادغام می‌شود (مقدار موجود اولویت دارد، فیلدهای خالی از Import پر می‌شوند)،
  Tags اجتماع دو مجموعه است، Category موجود حفظ می‌شود مگر خودش خالی باشد، و زبان‌هایی که
  فقط در یکی از دو نسخه هستند از بین نمی‌روند
- **Backup خودکار قبل از Restore** (بند 50): `AutoBackupWriter` قبل از هر `applyImport`
  واقعی یک Snapshot کامل از وضعیت فعلی در حافظه داخلی اپ ذخیره می‌کند (۴ نسخه آخر نگه داشته
  می‌شود)؛ اگر گرفتن این Snapshot با خطا مواجه شود، Import اصلاً اعمال نمی‌شود
- `BackupRestoreScreen`: دکمه «ادغام (Merge)» به Dialog تعارض اضافه شد؛ بعد از Import موفق
  مسیر نسخه پشتیبان خودکار به کاربر نشان داده می‌شود

### Gap باقی‌مانده
- خود فایل Export/Import که کاربر با SAF انتخاب می‌کند رمزنگاری نمی‌شود (فقط API Key در
  حافظه داخلی رمزنگاری‌شده است) — Encryption روی خود فایل Backup هنوز اضافه نشده
- Snapshot های خودکار Pre-Restore از داخل خود اپ قابل Restore نیستند (فقط ذخیره می‌شوند)؛
  در حال حاضر کاربر باید آن‌ها را با ابزار خارجی (مثل ADB) بازیابی کند — بهبود بعدی: اضافه
  کردن دکمه «بازیابی از Snapshot خودکار» در همین صفحه

---

## مرحله 15: فیلتر ترکیبی کامل در Review (جدید)
- `LearningStateDao` سه Query جدید با JOIN به `concepts` گرفت: `getDueForStageInCategory`,
  `getByDifficultyInCategory`, `getLearnedInCategory` — فیلتر Category مستقیماً در دیتابیس
  اعمال می‌شود، نه در حافظه بعد از خواندن کل نتیجه
- `LearningStateRepository.getDue/getByDifficulty/getLearned` همگی یک پارامتر اختیاری
  `categoryId` گرفتند (پیش‌فرض null = بدون فیلتر، سازگار با تمام Callerهای قبلی)
- **مسیر کامل بند 30** («سخت + سفر» یا «ماهانه + غذا» و ...): `ReviewTypeSelectScreen` حالا
  ابتدا یک ردیف Chip برای انتخاب Category (اختیاری) نشان می‌دهد، سپس نوع مرور را؛ هر دو با
  هم به `ReviewSessionViewModel` می‌رسند (`categoryId` به‌عنوان Query Param اختیاری در
  Route، مثل `review_session/HARD?categoryId=3`)
- کارت‌های Daily/Weekly/Monthly در Home Dashboard بدون تغییر مستقیم بدون فیلتر شروع می‌شوند
  (مسیر «شروع سریع»)؛ فیلتر ترکیبی فقط از تب Review در دسترس است، مطابق طراحی سند فاز صفر

### Gap باقی‌مانده
ترکیب با Tag (نه فقط Category) هنوز اضافه نشده — چون نیاز به UI چندانتخابی و Join با
`concept_tags` دارد؛ برای مرحله بعدی مرتبط با فیلترها ثبت شد. نام Category هم هنوز روی خود
فلش‌کارت نمایش داده نمی‌شود (از مرحله قبل باقی مانده).

---

## مرحله 14: مدیریت Category/Tag + فیلتر ترکیبی (جدید)
- `CategoryRepository`/`TagRepository` (Domain) + پیاده‌سازی در Data — مدیریت واقعی
  Category ها (بند 15) و دسترسی به Tag های موجود (بند 16)
- **ساخت Category جدید** مستقیماً از `AddManualScreen` و `ConceptDetailScreen` (فیلد متن +
  دکمه افزودن، بلافاصله به‌عنوان انتخاب‌شده فعال می‌شود)
- **Tags** به‌صورت فیلد متنی جدا با کاما در هر دو فرم افزودن دستی و ویرایش — تبدیل خودکار
  به رابطه Many-to-Many موجود (`ConceptTagEntity`)
- **فیلتر ترکیبی واقعی در Vocabulary List** (بند 30): Chip های Category بالای لیست +
  جستجوی متنی — هر دو همزمان کار می‌کنند (Query جدید `searchInCategory` در `ConceptDao`
  برای ترکیب صحیح Search + Category در یک درخواست دیتابیس، نه فیلتر روی نتیجه در حافظه)
- **نمایش Tags روی فلش‌کارت** هنگام Flip (بند 32) — `FlashcardView` اکنون `tags` می‌گیرد

### Gap های شناخته‌شده
- فیلتر ترکیبی هنوز فقط در Vocabulary List است؛ `ReviewTypeSelectScreen` هنوز امکان ترکیب
  Category/Tag با Stage/Difficulty را ندارد (مثال دقیق بند 30: «سخت + سفر + اسپانیایی» برای
  شروع یک جلسه مرور) — برای مرحله بعدی مرتبط با Review ثبت شد
- نام Category روی خود فلش‌کارت نمایش داده نمی‌شود (فقط Tags)؛ چون نیاز به Join اضافه با
  CategoryRepository در ReviewSessionViewModel دارد — بهبود کوچک بعدی
- افزودن Category/Tag در مسیرهای AI/Paste/Import File هنوز اضافه نشده (فقط Add Manual و ویرایش)

---

## مرحله 13: Edit/Delete/Favorite در Vocabulary List (جدید)
- `ConceptDao.setFavorite()` + `ConceptRepository.setFavorite()`: تغییر مستقیم Favorite بدون
  نیاز به خواندن/نوشتن کل Concept
- **Favorite در خود لیست** (Quick Action): ضربه روی آیکون ستاره در `VocabularyListScreen`
  بلافاصله (Optimistic Update محلی، بدون نیاز به Reload کل لیست) وضعیت را تغییر می‌دهد
- **ضربه روی هر ردیف** به `ConceptDetailScreen` می‌رود: فرم ویرایش کامل (متن/ترجمه/تلفظ/
  مثال/یادداشت) + دکمه ذخیره
- **حذف** با Dialog تأیید صریح: در واقع Archive است (`active=false`) نه حذف فیزیکی، تا
  تاریخچه مرور آن Concept برای همیشه باقی بماند (طبق بند 23 و 27)؛ بعد از حذف، آن Concept
  دیگر در Vocabulary List یا هیچ صف مروری ظاهر نمی‌شود (فیلتر `active=1` در تمام Query ها)
- Navigation کامل: `Routes.CONCEPT_DETAIL` با آرگومان `conceptId` (Long)

---

## مرحله 12: Import File — CSV/JSON (جدید)
- `ImportParsedEntriesUseCase` جدید: منطق مشترک ذخیره‌سازی برای هر جریانی که یک لیست
  `ParsedVocabularyEntry` تولید می‌کند؛ `AddPasteTextViewModel` هم برای رعایت بند 82
  (بدون Duplicate Code) به این UseCase مشترک منتقل شد
- `ParseCsvVocabularyUseCase` (+۴ تست JVM) و `ParseJsonVocabularyUseCase` (+۳ تست Instrumented
  چون org.json روی JVM خالص Stub است) — هر دو بند 43 را با فرمت‌های ساده source/target/notes
  پوشش می‌دهند
- `AddImportFileScreen`: انتخاب فایل واقعی با `ActivityResultContracts.OpenDocument()`
  (به‌جای `GetContent` تک‌نوعی، چون امکان چند MIME Type همزمان می‌دهد)، تشخیص خودکار
  CSV/JSON بر اساس پسوند فایل، و همان الگوی Preview Table قابل ویرایش/حذف قبل از Import
- کارت «Import فایل» در Add Home اکنون واقعاً فعال است

### Gap های شناخته‌شده
- CSV Parser خطی ساده است؛ کاما داخل فیلد با Quote (`"a, b"`) پشتیبانی نمی‌شود
- XLSX و SQLite (باقی‌مانده بند 43) نیاز به کتابخانه/Parser دودویی دارند و در مرحله جداگانه می‌آیند
- ردیف‌های Import File بر خلاف Paste Text قابل ویرایش Inline نیستند (فقط Include/Remove) —
  چون منبع از فایل خارجی است، ویرایش کامل معمولاً باید در خود فایل انجام شود؛ در صورت نیاز
  می‌توان همان UI ویرایشی Paste Text را اینجا هم فعال کرد

---

## مرحله 11: Paste Text Import (جدید)
- `ParsePasteTextUseCase` (Domain, خالص و تست‌شده با ۵ تست JVM): بلوک‌های جدا با خط خالی را
  Parse می‌کند، شماره‌گذاری ابتدایی (`1.` `2)` `3-`) را حذف می‌کند، خط اول=مبدا، خط دوم=ترجمه،
  خط سوم اختیاری=برچسب (به‌عنوان Notes ذخیره می‌شود)
- `AddPasteTextViewModel`/`AddPasteTextScreen`: Textarea → دکمه «تجزیه متن» → **Preview Table
  کاملاً قابل ویرایش** (هر ردیف قابل ویرایش/حذف/غیرفعال‌سازی با Checkbox) → «Import همه»
  (بند 42 — هیچ رکوردی بدون عبور از این Preview مستقیم ذخیره نمی‌شود)
- وصل شدن کارت «جای‌گذاری متن» در Add Home به این صفحه

### Gap شناخته‌شده
Parser فعلی قطعی (Deterministic, بدون AI) است و فرمت مشخصی انتظار دارد (بلوک ۲-۳ خطی جدا با
خط خالی). نسخه هوشمندتر که بتواند فرمت‌های نامنظم‌تر متن را هم تشخیص دهد (با کمک AI) در
مرحله بعدی‌های مرتبط با AI اضافه می‌شود.

---

## مرحله 10: AI Translation Service (جدید)
- `AITranslationService` (Domain) با Provider قابل تعویض (بند 76): `AIProvider` یک Interface
  است؛ `OpenAICompatibleProvider` پیاده‌سازی پیش‌فرض برای هر سرویس سازگار با Chat Completions
  است (بدون هیچ وابستگی شبکه‌ای اضافه — فقط `HttpURLConnection` داخلی JDK/Android)
- **API Key هرگز Hard-code نشده** (بند 76): فقط از صفحه AI Settings وارد و در دیتابیس محلی
  ذخیره می‌شود (نکته امنیتی زیر را ببینید)
- **Structured JSON Output** (بند 77): پرامپت مشخصاً خواستار `translation, pronunciation,
  partOfSpeech, definition, example, notes` است
- **Validate/Parse/Preview قبل از Save** (بند 78): `AITranslationServiceImpl` پاسخ خام را
  Parse و Validate می‌کند (فیلد `translation` باید غیرخالی باشد وگرنه Result.failure)؛ AI هرگز
  مستقیماً چیزی در دیتابیس نمی‌نویسد — این کار را `AddAIViewModel.approve()` فقط بعد از
  تأیید صریح کاربر انجام می‌دهد (بند 41)
- **Offline-First واقعی** (بند 3): `NetworkUtils` قبل از هر تماس AI اتصال اینترنت را چک می‌کند؛
  در نبود اینترنت یا نبود تنظیمات کامل، `Result.failure` با پیام واضح برمی‌گردد و بقیه اپلیکیشن
  بدون مشکل کار می‌کند
- `AddAIScreen`: متن مبدا → دکمه «ترجمه با AI» → Preview کاملاً قابل ویرایش → «تأیید و ذخیره» یا «لغو»
- `AISettingsScreen`: تنظیم Endpoint/Model/API Key (فیلد API Key به‌صورت Password مخفی نمایش داده می‌شود)

### نکته امنیتی شفاف (Gap)
مقادیر AI Settings در حال حاضر به‌صورت متن ساده در جدول `app_settings` ذخیره می‌شوند، نه در
Android Keystore/EncryptedSharedPreferences. برای نسخه Production باید این مقدار رمزنگاری شود؛
در این مرحله به‌عنوان بهبود امنیتی مرحله بعد ثبت شده، نه چیزی که در کد مخفی مانده باشد.

---

## مرحله 9: Import/Export JSON کامل (جدید)
- `BackupRepository` (Domain) + `JsonBackupServiceImpl` (Data): Export کامل دیتابیس به JSON
  شامل Languages, Categories, Concepts+Contents+Tags, LearningState, ReviewHistory, و Settings
  (بند 44) — Concept ها با `uuid` (نه id داخلی) شناسایی می‌شوند تا بین دستگاه‌ها قابل انتقال باشند
  (بند 11)، و Category با نام ارجاع داده می‌شود چون id بین دستگاه‌ها یکسان نیست
- **Duplicate Detection واقعی** (بند 46): `previewImport()` فقط می‌خواند و مقایسه می‌کند — چند مورد
  جدید، چند مورد کاملاً یکسان (Skip خودکار)، و چند مورد Conflict (uuid یکسان + محتوای متفاوت)
- **Conflict Resolution** (بند 47): `applyImport()` با `ConflictResolution` (KEEP_EXISTING /
  USE_IMPORTED / SKIP) — MERGE فیلد-به-فیلد پیاده‌سازی نشده (Gap زیر)
- `ReviewHistory` هرگز Overwrite نمی‌شود، فقط Append (طبق بند 27)؛ `ReviewSession` های ارجاع‌شده
  در صورت نبود ساخته می‌شوند تا Foreign Key نشکند
- `BackupRestoreScreen`: فایل واقعی با Storage Access Framework — Export مستقیم یک فایل JSON
  در محل انتخابی کاربر می‌سازد (قابل Share/انتقال به گوشی دیگر — بند 49)، Import ابتدا Preview
  را در یک Dialog نشان می‌دهد و فقط بعد از تأیید صریح کاربر اعمال می‌شود
- وصل شدن به Settings از طریق کارت «پشتیبان‌گیری و بازیابی»

### Gap های شناخته‌شده این مرحله
- استراتژی **Merge** فیلد-به-فیلد (یکی از ۴ گزینه بند ۴۷) پیاده نشده؛ فقط ۳ گزینه دیگر کار می‌کنند
- Export به فرمت‌های CSV/XLSX/SQLite (بند 44) هنوز اضافه نشده — فقط JSON
- Encryption روی فایل Backup (بند 50) اضافه نشده
- «Backup خودکار قبل از Restore» (بند 50) به‌صورت خودکار در UI انجام نمی‌شود؛ کاربر باید خودش
  قبل از Import یک Export جداگانه بگیرد

---

## مرحله 8: Settings + Statistics واقعی (جدید)
- افزودن فیلد `notes` به `ConceptEntity`/`Concept` (رفع Gap مرحله قبل) + وصل شدن به فرم Add Manual
- `SettingsViewModel` + `SettingsHomeScreen`: تغییر واقعی تم (Light/Dark/System — بند 6 و 54)
  که مستقیماً روی `FlashLearnTheme` در `MainActivity` اثر می‌گذارد، و تغییر جهت زبان فعال
  (برعکس کردن Source/Target بدون تغییر دیتای واژگان — بند 71) + دکمه بازگشت به Onboarding
  برای انتخاب یک جفت‌زبان کاملاً جدید
- `StatisticsViewModel` + `StatisticsScreen`: آمار واقعی از دیتابیس — تعداد کل واژگان، تعداد در هر
  Stage، خلاصه Difficulty، و درصد دقت در چهار بازه زمانی Today/This Week/This Month/All Time
  (بند 66-67) با استفاده مستقیم از `ReviewHistoryRepository`
- افزودن `getStageSummary()` به `LearningStateDao`/`LearningStateRepository` برای شمارش بر اساس Stage
- افزودن `DateTimeUtils.startOfDay()` برای بازه‌بندی دقیق آمار «امروز»
- لینک «مشاهده آمار کامل» در Home Dashboard به صفحه Statistics وصل شد

با این مرحله، تمام صفحات اصلی Bottom Navigation (Home, Review, Vocabulary, Add, Settings)
به‌همراه Statistics به‌طور کامل کار می‌کنند و به دیتابیس واقعی وصل‌اند.

---

## مرحله 7: Vocabulary List + Add Manual (جدید)
- `VocabularyListViewModel` + `VocabularyListScreen`: جستجوی واقعی (متن/ترجمه/مثال) با
  Pagination و Lazy Loading خودکار هنگام اسکرول (بند 38-39 و 57 — هیچ‌گاه کل دیتابیس یک‌جا Load نمی‌شود)
- `AddManualViewModel` + `AddManualScreen`: افزودن دستی کلمه با فیلدهای متن/ترجمه/تلفظ/مثال،
  ساخت خودکار `LearningState` اولیه (DAILY) تا کلمه بلافاصله وارد چرخه مرور شود (بند 40)
- `AddHomeScreen` اکنون واقعاً به Add Manual وصل است؛ گزینه‌های AI/Paste/Import غیرفعال نمایش داده
  می‌شوند (طبق اصل Offline-First تا پیاده‌سازی کامل در مرحله بعد)

### نکته شفاف (Gap شناخته‌شده)
بند 14 نیازمندی یک فیلد `Notes` مستقل برای هر Vocabulary Item خواسته؛ این فیلد فعلاً در
`ConceptEntity`/`Concept` وجود ندارد. به‌جای گذاشتن یک UI بی‌اثر برایش، در این مرحله حذفش کردم
و در مرحله بعد به‌عنوان تغییر Schema اضافه می‌شود (نسخه 1 هنوز منتشر نشده، پس مشکلی در تغییر نیست).

---

## مرحله 6: Review Session واقعی (Flashcard UI + اتصال به الگوریتم)
- `ReviewSessionViewModel`: بارگذاری صف کارت‌های مرور بر اساس نوع انتخاب‌شده (Daily/Weekly/Monthly/
  Difficulty ها/Learned/Random — بند 29-31)، فراخوانی واقعی `ProcessReviewAnswerUseCase` روی هر پاسخ،
  ذخیره `LearningState` جدید و ثبت `ReviewHistory` با `responseTimeMs` واقعی و `sessionId` مرتبط
- `FlashcardView`: کارت با Flip با لمس و Swipe افقی (راست=بلدم چپ=بلد نیستم) طبق بند 32-34،
  نمایش تلفظ/مثال/تعریف وقتی برگردانده شده
- `ReviewSessionScreen`: Progress Bar بالای صفحه با تعداد درست/غلط (بند 35)، دکمه‌های بزرگ و واضح
  «بلدم/بلد نیستم» که همیشه در کنار Swipe در دسترس‌اند، و صفحه پایان جلسه با خلاصه نتیجه
- اتصال کامل به Navigation: پایان جلسه به Home برمی‌گردد و Back Stack جلسه تمام‌شده را پاک می‌کند

با این مرحله، مسیر اصلی اپلیکیشن (Onboarding → Home → انتخاب نوع مرور → مرور واقعی با ذخیره در
دیتابیس → بازگشت به Home) کاملاً کاربردی و End-to-End است.

---

## مرحله 3: لایه Data + Dependency Injection (جدید)
- Mapper های دوطرفه Entity ↔ Domain: `ConceptMapper`, `LearningStateMapper`
- پیاده‌سازی کامل چهار Repository: `ConceptRepositoryImpl`, `LearningStateRepositoryImpl`,
  `ReviewHistoryRepositoryImpl`, `ReviewSessionRepositoryImpl`
- افزودن ستون `everFailed` به `LearningStateEntity` (برای تعیین صحیح Difficulty=EASY طبق بند 26 —
  این فیلد باید در دیتابیس ذخیره شود، نه از روی totalWrong حدس زده شود)
- Hilt Modules: `DatabaseModule` (Database + همه DAO ها) و `RepositoryModule` (Binding چهار Repository)
- `GenerateSessionIdUseCase` اکنون واقعاً Session id به فرمت `yyyy-MM-dd-NNN` تولید و ذخیره می‌کند
- **2 تست Instrumented جدید** (`RepositoryRoundTripTest`) که صحت Mapper ها را با Round-trip
  واقعی از دیتابیس تأیید می‌کنند (Concept با چند Content و Tag، و LearningState با everFailed)

با این مرحله، لایه‌های Database → Data → Domain کاملاً به هم متصل و تست‌شده‌اند.
چیزی که باقی می‌ماند فقط UI (Compose) و Navigation است.

---

## مرحله 2: لایه Domain
- Model های خالص Kotlin (بدون وابستگی به Android/Room):
  `LearningStage`, `Difficulty`, `ContentType`, `ContentItem`, `Concept`, `LearningState`, `ReviewOutcome`
- Repository Interface ها (پیاده‌سازی واقعی در مرحله بعد/لایه Data می‌آید):
  `ConceptRepository`, `LearningStateRepository`, `ReviewHistoryRepository`, `ReviewSessionRepository`
- **ProcessReviewAnswerUseCase**: پیاده‌سازی کامل و خالص الگوریتم مرور طبق بندهای 19 تا 26
  (شامل قانون EASY فقط برای مسیر بدون خطا، HARD/VERY_HARD بر اساس monthlyWrongCount، و رفتار مرور اختیاری LEARNED)
- `GetDueConceptsUseCase`, `GenerateSessionIdUseCase`
- **13 تست JVM** (بدون نیاز به Emulator، با `./gradlew test` اجرا می‌شوند) که تمام حالت‌های
  الگوریتم را پوشش می‌دهند: Daily/Weekly/Monthly صحیح و غلط، شمارش خطاهای Monthly،
  رفتار Learned، و دو سناریوی زنجیره‌ای کامل (مسیر بدون خطا تا EASY، و مسیر با یک شکست در Monthly).

اجرای این تست‌ها (سریع‌تر از تست‌های Instrumented مرحله قبل، چون نیازی به Android/Emulator ندارند):
```
./gradlew test --tests "com.app.flashlearn.domain.usecase.ProcessReviewAnswerUseCaseTest"
```

---

## مرحله 1: لایه دیتابیس

این مرحله شامل موارد زیر است:

## چه چیزی ساخته شده
- پیکربندی کامل Gradle (root + app) با Kotlin, Compose, Room, Hilt, KSP
- تمام Entity های Room طبق Schema تأییدشده (Language, Concept, Content, LearningState,
  ReviewHistory, ReviewSession, Category, Tag, ConceptTag, LanguagePair, AppSettings)
- تمام DAO های مربوطه با Query های لازم برای فیلتر due-date (بند 20/22/31 نیازمندی‌ها)
- کلاس FlashLearnDatabase (نسخه 1) + ساختار Migrations برای توسعه آینده
- Utility های پایه (DateTimeUtils, Constants برای Stage/Difficulty/ContentType)
- FlashLearnApplication (Hilt) و MainActivity موقت (فقط برای تضمین Build شدن پروژه)
- تست‌های Instrumented (Room in-memory) برای:
  - درج Concept + چند Content و بازخوانی صحیح
  - یکتا بودن uuid
  - جستجو در متن ترجمه‌شده
  - فیلتر due-date برای Weekly/Monthly
  - شمارش آیتم‌های آماده مرور
  - خلاصه Difficulty
  - عدم از دست رفتن Review History و ترتیب صحیح آن

## چگونه Build و اجرا شود
```
./gradlew assembleDebug
./gradlew connectedAndroidTest   # اجرای تست‌های Room روی دستگاه/Emulator
```

## ساختار پروژه (تا این مرحله)
```
app/src/main/java/com/app/flashlearn/
├── FlashLearnApplication.kt
├── MainActivity.kt
├── core/util/ (DateTimeUtils, Constants)
└── database/
     ├── entity/ (11 Entity)
     ├── dao/ (10 DAO)
     ├── migration/Migrations.kt
     └── FlashLearnDatabase.kt

app/src/androidTest/java/com/app/flashlearn/database/
├── DatabaseTestUtil.kt
├── ConceptDaoTest.kt
├── LearningStateDaoTest.kt
└── ReviewHistoryDaoTest.kt
```

## نکات مهم طراحی که در کد رعایت شده
- Concept و Content کاملاً جدا هستند؛ هیچ ستون ثابتی مثل `spanish_text` وجود ندارد.
- `uuid` روی Concept یکتا و Indexed است — برای Import/Export بدون Collision.
- Index های لازم روی `stage`, `difficulty`, `nextReviewAt`, `languageCode`, `active` و
  `categoryId` برای Performance با دیتابیس بزرگ (طبق بند 57/58) اضافه شده‌اند.
- Pagination در ConceptDao با `limit`/`offset` رعایت شده — هیچ Query کل دیتابیس را یک‌جا Load نمی‌کند.
- Review History هیچ‌گاه حذف نمی‌شود (فقط Insert دارد، بدون Delete/Update).
- fallbackToDestructiveMigration در هیچ‌جا استفاده نشده — Migration باید صریح نوشته شود.

## چیزی که هنوز ساخته نشده (مراحل بعدی)
- Import XLSX/SQLite (بند 43) و Export CSV/XLSX/SQLite (بند 44)
- Encryption روی خود فایل Backup + دکمه Restore از Snapshot خودکار داخل اپ
- ترکیب فیلتر با Tag (نه فقط Category)، نمایش نام Category روی فلش‌کارت
- مهاجرت کامل بقیه صفحات به strings.xml (کار مکانیکی باقی‌مانده)
- بررسی نهایی Edge Case های بند 64 (تغییر Timezone، بستن اپ وسط Review، Import ناقص)

## روند ادامه کار
مرحله بعدی: مرور نهایی روی Edge Case های بند 64 که هنوز صریحاً تست/بررسی نشده‌اند،
به‌همراه یک جمع‌بندی کامل مستندات پروژه.
