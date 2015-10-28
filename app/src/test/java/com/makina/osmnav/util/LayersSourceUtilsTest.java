package com.makina.osmnav.util;

import com.makina.osmnav.TestHelper;
import com.makina.osmnav.model.LayersSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link LayersSourceUtils} class.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@RunWith(RobolectricTestRunner.class)
public class LayersSourceUtilsTest {

    @Test
    public void testReadLayersSource() throws
                                       Exception {
        final InputStream inputStream = TestHelper.getFixtureAsStream("gdl.json");

        assertNotNull(inputStream);

        final LayersSource layersSource = LayersSourceUtils.readLayersSource(new InputStreamReader(inputStream));

        assertNotNull(layersSource);
        assertEquals("Paris Gare de Lyon",
                     layersSource.name);

        assertNotNull(layersSource.boundingBoxE6);
        assertEquals(Double.valueOf(48.8487d * 1E6),
                     Double.valueOf(layersSource.boundingBoxE6.getLatNorthE6()));
        assertEquals(Double.valueOf(2.3866d * 1E6),
                     Double.valueOf(layersSource.boundingBoxE6.getLonEastE6()));
        assertEquals(Double.valueOf(48.8368d * 1E6),
                     Double.valueOf(layersSource.boundingBoxE6.getLatSouthE6()));
        assertEquals(Double.valueOf(2.3664d * 1E6),
                     Double.valueOf(layersSource.boundingBoxE6.getLonWestE6()));

        assertNotNull(layersSource.layerSource);
        assertEquals("gdl.mbtiles",
                     layersSource.layerSource.base);
        assertFalse(layersSource.layerSource.layers.isEmpty());
        assertFalse(layersSource.layerSource.levels.isEmpty());
        Assert.assertEquals(Arrays.asList("gdl_2.0.mbtiles",
                                          "gdl_1.0.mbtiles",
                                          "gdl_0.0.mbtiles",
                                          "gdl_-0.25.mbtiles",
                                          "gdl_-0.5.mbtiles",
                                          "gdl_-0.75.mbtiles",
                                          "gdl_-1.0.mbtiles",
                                          "gdl_-2.0.mbtiles",
                                          "gdl_-3.0.mbtiles"),
                            layersSource.layerSource.layers);
        Assert.assertEquals(Arrays.asList(2.0d,
                                          1.0d,
                                          0.0d,
                                          -0.25d,
                                          -0.5d,
                                          -0.75d,
                                          -1.0d,
                                          -2.0d,
                                          -3.0d),
                            new ArrayList<>(layersSource.layerSource.levels));
    }
}