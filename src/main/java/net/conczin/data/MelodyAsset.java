package net.conczin.data;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MelodyAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, MelodyAsset>> {
    public static final AssetBuilderCodec<String, MelodyAsset> CODEC = AssetBuilderCodec.builder(
                    MelodyAsset.class,
                    MelodyAsset::new,
                    Codec.STRING,
                    (t, id) -> t.id = id,
                    t -> t.id,
                    (t, data) -> t.data = data,
                    t -> t.data
            )
            .appendInherited(
                    new KeyedCodec<>("Name", Codec.STRING),
                    (item, v) -> item.name = v,
                    item -> item.name,
                    (item, parent) -> item.name = parent.name
            )            
            .add()
            // TODO: IMPLEMENT SUPPORT FOR CHOICE TRIM
            // New JSON value which tells the parser to trim whitespace at the front of the MIDI.
            //.appendInherited(
        		//	new KeyedCodec<>("TrimMIDI", Codec.BOOLEAN),
            //   (item, v) -> item.offsetNotes = v,
            //   item -> item.offsetNotes,
            //    (item, parent) -> item.offsetNotes = parent.offsetNotes
        		//)
            //.add()
            .afterDecode((o, extrainfo) -> {
                if (o.id != null && extrainfo instanceof AssetExtraInfo<?> assetExtraInfo) {
                    Path path = assetExtraInfo.getAssetPath();
                    if (path == null) return;
                    Path basePath = path.getParent().resolve(path.getFileName().toString().replaceFirst("\\.json$", ""));
                    // CHECK FOR EXISTENCE OF .MIDI AND .MID FILES
                    List<Path> midiPaths = List.of(
                    			basePath.resolveSibling(basePath.getFileName().toString() + ".midi"),
                    			basePath.resolveSibling(basePath.getFileName().toString() + ".mid")
                    		);
                    IOException prevException = null;
                    for (Path midiPath : midiPaths) {
                    	try (InputStream midiStream = Files.newInputStream(midiPath)) {
                            o.melody = new Melody(o.name, MidiParser.parseMidi(midiStream));
                            return;
                        } catch (IOException e) {
                        		if (prevException == null) {
                        			prevException = e;
                        		}
                        		else {
                        			throw new RuntimeException(e);
                        		}
                        }
                    }
                }
            })
            .build();


    private static AssetStore<String, MelodyAsset, DefaultAssetMap<String, MelodyAsset>> ASSET_STORE;

    public static AssetStore<String, MelodyAsset, DefaultAssetMap<String, MelodyAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(MelodyAsset.class);
        }
        return ASSET_STORE;
    }

    private String id;
    private AssetExtraInfo.Data data;

    private String name;
    //private boolean offsetNotes; // New JSON value which tells the parser to trim whitespace at the front of the MIDI.
    private Melody melody;

    @Override
    public String getId() {
        return id;
    }

    public Melody getMelody() {
        return melody;
    }
}
