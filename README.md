# FlashLearn - Android Flashcard Learning App

پروژه کامل اپلیکیشن اندروید یادگیری با کارت‌های فلش بر اساس Clean Architecture.

## ساختار پروژه
- **Clean Architecture** با لایه‌های Domain / Data / Presentation
- **Room Database** برای ذخیره‌سازی محلی
- **Hilt** برای Dependency Injection
- **Jetpack Compose + Material 3**
- **Coroutines + Flow**
- الگوریتم مرور فاصله‌دار (Spaced Repetition) سفارشی

## نحوه استفاده
1. فایل ZIP را دانلود و استخراج کنید.
2. پروژه را در Android Studio باز کنید (Open an Existing Project).
3. Gradle Sync را اجرا کنید.
4. روی یک امولاتور یا دستگاه واقعی Build & Run کنید.

## نکات
- این کدها بر اساس مشخصات ارائه شده نوشته شده‌اند.
- برخی صفحات (مثل Add، Settings کامل، Statistics) اسکلت هستند و می‌توانید آن‌ها را گسترش دهید.
- برای تولید واقعی، Migrationهای Room را جایگزین `fallbackToDestructiveMigration` کنید.
- آیکون‌های launcher (mipmap) وجود ندارند؛ Android Studio به‌صورت پیش‌فرض آن‌ها را مدیریت می‌کند یا خودتان اضافه کنید.

ساخته شده برای سهولت دانلود و استفاده روی موبایل.
