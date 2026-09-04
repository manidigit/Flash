# FlashLearn — خروجی دور اسناد معماری (۰۱ تا ۱۴)

این پوشه حاصل تجمیع و رفع‌تکرارِ همه‌ی کدهای نمونه‌ای است که در اسناد ۰۱ تا ۱۴ تولید شدند.
**این کد تست کامپایل نشده** (چون این محیط دسترسی به Android SDK/Gradle واقعی ندارد) —
باید در Termux یا Android Studio واقعاً build بشه و خطاهای احتمالی برطرف بشن.

## قبل از استفاده حتماً بخون

۱. **AI Translation Service** (`data/ai/AITranslationServiceImpl.kt`) کاملاً حدسی و تست‌نشده است.
   باید provider واقعی مشخص بشه و با کلید واقعی تست بشه.

۲. **Migration های Room واقعی وجود ندارند** — فقط ورژن ۱ دیتابیس تعریف شده. هر تغییر schema بعدی
   نیاز به یک Migration صریح داره وگرنه داده کاربر از بین می‌ره.

۳. **فونت Vazirmatn اضافه نشده** — باید فایل فونت رو دستی در `res/font/` بذاری و در
   `ui/theme/Theme.kt` به Typography وصلش کنی.

۴. **match کردن دسته‌بندی هنگام Import کلمات** (`data/backup/JsonBackupServiceImpl.kt`) ناقصه —
   فعلاً `categoryId = null` هنگام import.

۵. **isFirstLaunch در MainActivity.kt هاردکد شده به false** — باید از `AppSettingsRepository`
   یا یک DataStore جدا خونده بشه.

۶. **تست‌های Unit فقط placeholder هستن** (`app/src/test/`) — نیاز به Fake Repository ها دارن.

## کارهایی که اصلاً در این دور اسناد لمس نشدن (طبق حافظه پروژه)

- استخراج پرانتزی متن به notes + دکمه رفرش (صفحه Vocabulary)
- Hint button در حالت چندگزینه‌ای
- فیکس چیدمان صفحه Paste-text وقتی کیبورد بازه
- Tag-based filtering پیشرفته + نمایش دسته‌بندی روی خود فلش‌کارت
- XLSX/SQLite Export
- حالت‌های AI/Paste/Import کامل در AddConceptScreen (فقط Manual + دکمه AI ساده)

## ساختار

```
app/src/main/java/com/app/flashlearn/
├── database/       (Entity, DAO, Database)
├── domain/         (Model, Repository interface, Service interface, UseCase)
├── data/           (Mapper, Repository impl, AI, Backup)
├── di/             (Hilt Modules)
├── presentation/   (ViewModel + Screen به تفکیک فیچر)
├── ui/theme/       (رنگ‌ها و Theme طبق Design System)
└── navigation/     (NavGraph، BottomBar، Screen routes)
```

## مرجع کامل تصمیم‌ها و نکات باز

برای جزئیات کامل هر تصمیم (چرا uuid به‌جای id، چرا فیلتر client-side به Query تبدیل شد، و غیره)
به اسناد `01` تا `14` (که جدا از این zip در چت موجودن) مراجعه کن — این‌ها توضیح می‌دن *چرا* هر
تصمیمی گرفته شده، نه فقط *چی* نوشته شده.
