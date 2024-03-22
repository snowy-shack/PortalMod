package net.portalmod.core.chunkviewer;

import net.minecraft.util.math.ChunkPos;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChunkViewer {
    private static final ChunkViewer INSTANCE = new ChunkViewer();

    private final JFrame window;
    protected final List<ChunkPos> chunks = new ArrayList<>();

    public ChunkViewer() {
        System.setProperty("java.awt.headless", "false");
        window = new JFrame("Chunk Viewer");
        window.setContentPane(new ChunkViewPane());
        window.getContentPane().setPreferredSize(new Dimension(300, 300));
        window.pack();
        window.setResizable(false);

//        chunks.add(new ChunkPos(0, 0));
//        chunks.add(new ChunkPos(10, 10));
//        chunks.add(new ChunkPos(-3, -5));
    }

    private class ChunkViewPane extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            for(ChunkPos pos : chunks.toArray(new ChunkPos[0])) {
                g.fillRect(pos.x * 1 + 150, pos.z * 1 + 150, 1, 1);
            }
        }
    }

    public void setVisible(boolean visible) {
        window.setVisible(visible);
    }

    public void refresh() {
        window.repaint();
    }

    public List<ChunkPos> getChunkList() {
        return chunks;
    }

    public static ChunkViewer getInstance() {
        return INSTANCE;
    }
}