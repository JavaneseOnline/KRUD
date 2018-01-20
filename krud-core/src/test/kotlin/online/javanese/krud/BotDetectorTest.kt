package online.javanese.krud

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import online.javanese.krud.stat.HitStat
import org.junit.Test


class BotDetectorTest {

    @Test fun testBotDetector() {
        val ib = HitStat.IsBot
        assertTrue(ib("Slackbot"))
        assertTrue(ib("SlAcKbOt"))
        assertTrue(ib("SlAcKbOt/1"))
        assertTrue(ib("Slackbot/100500.200700"))
        assertTrue(ib("fdskfjmd,cxbot"))
        assertFalse(ib("notabotReally"))
    }

}
