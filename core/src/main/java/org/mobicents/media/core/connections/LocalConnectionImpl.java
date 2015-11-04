/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.core.connections;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.mobicents.media.server.component.audio.AudioComponent;
import org.mobicents.media.server.component.oob.OOBComponent;
import org.mobicents.media.server.impl.rtp.ChannelsManager;
import org.mobicents.media.server.impl.rtp.LocalDataChannel;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.ConnectionType;
import org.mobicents.media.server.utils.Text;

/**
 *
 * @author yulian oifa
 */
public class LocalConnectionImpl extends BaseConnection {
    
    private static final Logger LOGGER = Logger.getLogger(LocalConnectionImpl.class);

    private final LocalDataChannel localAudioChannel;

    public LocalConnectionImpl(int id, ChannelsManager channelsManager) {
        super(id, channelsManager.getScheduler());
        this.localAudioChannel = channelsManager.getLocalChannel();
    }
    
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.LOCAL;
    }
    
    @Override
    public boolean getIsLocal() {
        return false;
    }

    /**
     * Sets whether connection should be bound to local or remote interface.
     * <p>
     * <b>Supported only for RTP connections.</b>
     * </p>
     */
    @Override
    public void setIsLocal(boolean isLocal) {
        // do nothing
    }

    @Override
    public AudioComponent getAudioComponent() {
        return this.localAudioChannel.getAudioComponent();
    }

    @Override
    public OOBComponent getOOBComponent() {
        return this.localAudioChannel.getOOBComponent();
    }

    @Override
    public void generateOffer(boolean webrtc) throws IOException {
        throw new UnsupportedOperationException("Not supported yet!");
    }

    @Override
    public void setOtherParty(Connection other) throws IOException {
        if (!(other instanceof LocalConnectionImpl)) {
            throw new IOException("Not compatible");
        }

        this.localAudioChannel.join(((LocalConnectionImpl) other).localAudioChannel);

        try {
            open();
            ((LocalConnectionImpl) other).open();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void setOtherParty(Text descriptor) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOtherParty(byte[] descriptor) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getPacketsReceived() {
        return 0L;
    }

    @Override
    public long getBytesReceived() {
        return 0L;
    }

    @Override
    public long getPacketsTransmitted() {
        return 0L;
    }

    @Override
    public long getBytesTransmitted() {
        return 0L;
    }

    @Override
    public double getJitter() {
        return 0.0;
    }

    @Override
    public void setMode(ConnectionMode mode) {
        if (this.localAudioChannel.isJoined()) {
            localAudioChannel.updateMode(mode);
        }
        super.setMode(mode);
    }
    
    @Override
    protected void onFailed() {
        setMode(ConnectionMode.INACTIVE);
        this.localAudioChannel.unjoin();
        
        if (this.connectionFailureListener != null) {
            this.connectionFailureListener.onFailure();
        }
        this.connectionFailureListener = null;
    }

    @Override
    protected void onClosed() {
        setMode(ConnectionMode.INACTIVE);
        this.localAudioChannel.unjoin();
        this.connectionFailureListener = null;
    }

    public boolean isAvailable() {
        // TODO What is criteria for this type of channel to be available
        return true;
    }

    /*
     * POOL RESOURCE
     */
    @Override
    public void checkIn() {
        close();
        reset();
    }
    
    @Override
    public void checkOut() {
        // TODO Auto-generated method stub
    }

    @Override
    public String getLocalDescriptor() {
        return "";
    }

    @Override
    public String getRemoteDescriptor() {
        return "";
    }
}
