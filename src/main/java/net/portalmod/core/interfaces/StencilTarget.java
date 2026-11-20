package net.portalmod.core.interfaces;

/**
 * A framebuffer that can carry a stencil buffer (Forge added RenderTarget#enableStencil; vanilla
 * 1.20.1 has none). Implemented on RenderTarget by FramebufferMixin. Portal rendering needs it.
 */
public interface StencilTarget {
    /** Turns the stencil buffer on and recreates the buffers. */
    void pmEnableStencil();

    boolean pmIsStencilEnabled();
}
