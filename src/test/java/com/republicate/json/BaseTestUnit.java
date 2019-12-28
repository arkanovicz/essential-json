package com.republicate.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTestUnit
{
    protected static final int MAX_OUTPUT = 100;
    protected static Logger logger = LoggerFactory.getLogger("json");

    protected long startNanos = 0L;
    protected double elapsedMillis = 0L;

    protected void log(String format, Object... objects)
    {
        logger.info(format, objects);
    }

    protected int checksum(String str)
    {
        int ret = 0;
        boolean insideString = false;
        boolean escaped = false;
        for (char c : str.toCharArray())
        {
            if (insideString)
            {
                if (escaped) escaped = false;
                else if (c == '"') insideString = false;
            }
            else if (c == '"') insideString = true;
            if (insideString || !Character.isWhitespace(c))
            {
                ret ^= c;
            }
        }
        return ret;
    }

    protected void startTiming()
    {
        startNanos = System.nanoTime();
    }

    protected void stopTiming()
    {
        elapsedMillis = (System.nanoTime() - startNanos)/1000000.0;
    }

    protected String elapsed()
    {
        return String.format("%.2fms", elapsedMillis);
    }
}
