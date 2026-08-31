# FlashLearn Stage 67 implementation

Applied to the supplied `flashlearn-stage66-database-fixed.zip`.

Implemented:
- Initial language set limited to fa/en/es with flags.
- Home language reverse button.
- Settings language reverse button.
- Real streak derived from review-history calendar days; no hard-coded 30.
- Weekly/monthly total and due counts.
- Practiced / unpracticed / learned counts.
- Learned excluded from review queues.
- Concrete nextReviewAt remains non-null.
- Initial language seeder.

Important:
The supplied Stage 66 archive is a database/UI slice and does not contain a complete Gradle project
or all application/navigation/DI sources. Therefore this archive is a source update package, not a
verified standalone Android build. It is structured under `flashlearn/` so it can be merged with the
existing `~/Flash` tree using the user's copy command.
