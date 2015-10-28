package com.makina.osmnav.util;

import com.makina.osmnav.TestHelper;
import com.makina.osmnav.model.LayerSource;
import com.makina.osmnav.model.LayersSettings;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link LayersSettingsUtils} class.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class LayersSettingsUtilsTest {

    @Test
    public void testReadLayersSettings() throws
                                         Exception {
        final InputStream inputStream = TestHelper.getFixtureAsStream("gdl.json");

        assertNotNull(inputStream);

        final LayersSettings layersSettings = LayersSettingsUtils.readLayersSettings(new InputStreamReader(inputStream));

        assertNotNull(layersSettings);
        assertEquals("Paris Gare de Lyon",
                     layersSettings.name);

        assertNotNull(layersSettings.boundingBoxE6);
        assertEquals(Double.valueOf(48.8487d * 1E6),
                     Double.valueOf(layersSettings.boundingBoxE6.getLatNorthE6()));
        assertEquals(Double.valueOf(2.3866d * 1E6),
                     Double.valueOf(layersSettings.boundingBoxE6.getLonEastE6()));
        assertEquals(Double.valueOf(48.8368d * 1E6),
                     Double.valueOf(layersSettings.boundingBoxE6.getLatSouthE6()));
        assertEquals(Double.valueOf(2.3664d * 1E6),
                     Double.valueOf(layersSettings.boundingBoxE6.getLonWestE6()));

        assertNotNull(layersSettings.layersSource);
        assertEquals(new LayerSource("gdl.mbtiles"),
                     layersSettings.layersSource.base);
        assertFalse(layersSettings.layersSource.layers.isEmpty());
        Assert.assertEquals(Arrays.asList(new LayerSource("gdl_2.0.mbtiles",
                                                          2d),
                                          new LayerSource("gdl_1.0.mbtiles",
                                                          1d),
                                          new LayerSource("gdl_0.0.mbtiles",
                                                          0d),
                                          new LayerSource("gdl_-0.25.mbtiles",
                                                          -0.25d),
                                          new LayerSource("gdl_-0.5.mbtiles",
                                                          -0.5d),
                                          new LayerSource("gdl_-0.75.mbtiles",
                                                          -0.75d),
                                          new LayerSource("gdl_-1.0.mbtiles",
                                                          -1d),
                                          new LayerSource("gdl_-2.0.mbtiles",
                                                          -2d),
                                          new LayerSource("gdl_-3.0.mbtiles",
                                                          -3d)),
                            layersSettings.layersSource.layers);
    }
}