package com.lemondronor.turboshrimp.xuggler;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

import com.lemondronor.turboshrimp.xuggler.ImageListener;
import com.lemondronor.turboshrimp.xuggler.VideoDecoder;


public class XugglerDecoder implements VideoDecoder
{
    private ImageListener listener;
    private boolean doStop = false;

    public void decode(InputStream is)
    {
        // Let's make sure that we can actually convert video pixel formats.
        if (!IVideoResampler.isSupported(
                IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
            throw new RuntimeException(
                "you must install the GPL version of Xuggler (with IVideoResampler support) for this to work");

        // Create a Xuggler container object
        IContainer container = IContainer.make();

        // Open up the container
        if (container.open(is, null) < 0)
            throw new IllegalArgumentException("could not open inputstream");

        // query how many streams the call to open found
        int numStreams = container.getNumStreams();
        // and iterate through the streams to find the first video stream
        int videoStreamId = -1;
        IStreamCoder videoCoder = null;
        for (int i = 0; i < numStreams; i++) {
            // Find the stream object
            IStream stream = container.getStream(i);
            // Get the pre-configured decoder that can decode this stream;
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videoStreamId = i;
                videoCoder = coder;
                break;
            }
        }
        if (videoStreamId == -1)
            throw new RuntimeException("could not find video stream");

        /*
         * Now we have found the video stream in this file. Let's open up our
         * decoder so it can do work.
         */
        if (videoCoder.open() < 0)
            throw new RuntimeException(
                "could not open video decoder for container");

        IVideoResampler resampler = null;
        if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
            // if this stream is not in BGR24, we're going to need to
            // convert it. The VideoResampler does that for us.
            resampler = IVideoResampler.make(
                videoCoder.getWidth(), videoCoder.getHeight(),
                IPixelFormat.Type.BGR24, videoCoder.getWidth(),
                videoCoder.getHeight(), videoCoder.getPixelType());
            if (resampler == null)
                throw new RuntimeException(
                    "could not create color space resampler.");
        }

        /*
         * Now, we start walking through the container looking at each
         * packet.
         */
        IPacket packet = IPacket.make();
        long firstTimestampInStream = Global.NO_PTS;
        long systemClockStartTime = 0;
        while (!doStop && container.readNextPacket(packet) >= 0) {
            /*
             * Now we have a packet, let's see if it belongs to our
             * video stream
             */
            if (packet.getStreamIndex() == videoStreamId) {
                /*
                 * We allocate a new picture to get the data out of
                 * Xuggler
                 */
                IVideoPicture picture = IVideoPicture.make(
                    videoCoder.getPixelType(), videoCoder.getWidth(),
                    videoCoder.getHeight());

                try {
                    int offset = 0;
                    while (offset < packet.getSize()) {
                        /*
                         * Now, we decode the video, checking for any errors.
                         */
                        int bytesDecoded = videoCoder.decodeVideo(
                            picture, packet, offset);
                        if (bytesDecoded < 0)
                            throw new RuntimeException(
                                "got an error decoding single video frame");
                        offset += bytesDecoded;

                        /*
                         * Some decoders will consume data in a
                         * packet, but will not be able to construct a
                         * full video picture yet. Therefore you
                         * should always check if you got a complete
                         * picture from the decoder
                         */
                        if (picture.isComplete()) {
                            IVideoPicture newPic = picture;
                            /*
                             * If the resampler is not null, that
                             * means we didn't get the video in BGR24
                             * format and need to convert it into
                             * BGR24 format.
                             */
                            if (resampler != null) {
                                // we must resample
                                newPic = IVideoPicture.make(
                                    resampler.getOutputPixelFormat(),
                                    picture.getWidth(), picture.getHeight());
                                if (resampler.resample(newPic, picture) < 0)
                                    throw new RuntimeException(
                                        "could not resample video");
                            }
                            if (newPic.getPixelType() !=
                                IPixelFormat.Type.BGR24)
                                throw new RuntimeException(
                                    "could not decode video as BGR 24 bit data");

                            // And finally, convert the BGR24 to an
                            // Java buffered image
                            // System.out.println("3 create
                            // BufferedImage");
                            BufferedImage javaImage = Utils.videoPictureToImage(newPic);

                            // and display it on the Java Swing window
                            if (listener != null)
                                listener.imageUpdated(javaImage);
                        }
                    } // end of while
                }
                catch (Exception exc) {
                    System.out.println("OUCH");
                    exc.printStackTrace();
                    // hopefully nothing really bad (probably failed to decode single video frame)
                    //					System.err.println("XugglerDecoder: Exception while decoding video: " + exc.getMessage());
                    //					exc.printStackTrace();
                }
            } else {
                // System.out.println("Packet does not belong to video stream");
                /*
                 * This packet isn't part of our video stream, so we just
                 * silently drop it.
                 */
                do {} while (false);
            }
        }
        System.out.println("XugglerDecoder: clean up and close stream ...");
        /*
         * Technically since we're exiting anyway, these will be
         * cleaned up by the garbage collector... but because we're
         * nice people and want to be invited places for Christmas,
         * we're going to show how to clean up.
         */
        if (videoCoder != null) {
            videoCoder.close();
            videoCoder = null;
        }
        if (container != null) {
            container.close();
            container = null;
        }
    }

    public void stop()
    {
        doStop = true;
    }

    public void setImageListener(ImageListener listener)
    {
        this.listener = listener;
    }
}
