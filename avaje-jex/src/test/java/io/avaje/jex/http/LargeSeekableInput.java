package io.avaje.jex.http;

import java.io.InputStream;

class LargeSeekableInput extends InputStream {
    private final long prefixSize;
    private final long contentSize;
    private long alreadyRead = 0L;

    public LargeSeekableInput(long prefixSize, long contentSize) {
        this.prefixSize = prefixSize;
        this.contentSize = contentSize;
    }

    private long remaining() {
        return prefixSize + contentSize - alreadyRead;
    }

    @Override
    public int available() {
        long rem = remaining();
        if (rem >= 0 && rem <= Integer.MAX_VALUE) {
            return (int) rem;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public long skip(long toSkip) {
        if (toSkip <= 0) {
            return 0;
        } else {
            long rem = remaining();
            if (rem >= 0 && rem <= toSkip) {
                alreadyRead += rem;
                return rem;
            } else {
                alreadyRead += toSkip;
                return toSkip;
            }
        }
    }

    @Override
    public int read() {
        if (remaining() == 0L) {
            return -1;
        } else if (alreadyRead < prefixSize) {
            alreadyRead++;
            return ' ';
        } else {
            alreadyRead++;
            return 'J';
        }
    }
}