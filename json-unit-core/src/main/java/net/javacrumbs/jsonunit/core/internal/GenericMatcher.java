package net.javacrumbs.jsonunit.core.internal;

interface GenericMatcher {
    Result matches(Context context);

    abstract class Result {
        private Result() {}

        static StopProcessing stopProcessing() {
            return new StopProcessing();
        }

        static ContinueProcessing continueProcessing() {
            return new ContinueProcessing();
        }

        static MismatchFound mismatchFound(String mismatch) {
            return new MismatchFound(mismatch);
        }

        static final class StopProcessing extends Result {
            private StopProcessing() {}
        }

        static final class ContinueProcessing extends Result {
            private ContinueProcessing() {}
        }

        static final class MismatchFound extends Result {
            private final String mismatch;

            private MismatchFound(String mismatch) {
                this.mismatch = mismatch;
            }

            public String getMismatch() {
                return mismatch;
            }
        }
    }
}
