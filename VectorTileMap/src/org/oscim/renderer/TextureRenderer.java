/*
 * Copyright 2012 Hannes Janetzek
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.oscim.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import org.oscim.renderer.layer.Layer;
import org.oscim.renderer.layer.TextureLayer;
import org.oscim.utils.GlUtils;

import android.opengl.GLES20;

public class TextureRenderer {
	private static int mTextureProgram;
	private static int hTextureMVMatrix;
	private static int hTextureProjMatrix;
	private static int hTextureVertex;
	private static int hTextureScale;
	private static int hTextureScreenScale;
	private static int hTextureTexCoord;
	private static int mIndicesVBO;

	final static int INDICES_PER_SPRITE = 6;
	final static int VERTICES_PER_SPRITE = 4;
	final static int SHORTS_PER_VERTICE = 6;
	// per texture
	public final static int MAX_ITEMS = 40;

	static void init() {
		mTextureProgram = GlUtils.createProgram(Shaders.textVertexShader,
				Shaders.textFragmentShader);

		hTextureMVMatrix = GLES20.glGetUniformLocation(mTextureProgram, "u_mv");
		hTextureProjMatrix = GLES20.glGetUniformLocation(mTextureProgram, "u_proj");
		hTextureScale = GLES20.glGetUniformLocation(mTextureProgram, "u_scale");
		hTextureScreenScale = GLES20.glGetUniformLocation(mTextureProgram, "u_swidth");
		hTextureVertex = GLES20.glGetAttribLocation(mTextureProgram, "vertex");
		hTextureTexCoord = GLES20.glGetAttribLocation(mTextureProgram, "tex_coord");

		int bufferSize = MAX_ITEMS * VERTICES_PER_SPRITE
				* SHORTS_PER_VERTICE * (Short.SIZE / 8);

		ByteBuffer buf = ByteBuffer.allocateDirect(bufferSize)
				.order(ByteOrder.nativeOrder());

		ShortBuffer mShortBuffer = buf.asShortBuffer();

		// Setup triangle indices
		short[] indices = new short[MAX_ITEMS * INDICES_PER_SPRITE];
		int len = indices.length;
		short j = 0;
		for (int i = 0; i < len; i += INDICES_PER_SPRITE, j += VERTICES_PER_SPRITE) {
			indices[i + 0] = (short) (j + 0);
			indices[i + 1] = (short) (j + 1);
			indices[i + 2] = (short) (j + 2);
			indices[i + 3] = (short) (j + 2);
			indices[i + 4] = (short) (j + 3);
			indices[i + 5] = (short) (j + 0);
		}

		mShortBuffer.clear();
		mShortBuffer.put(indices, 0, len);
		mShortBuffer.flip();

		int[] mVboIds = new int[1];
		GLES20.glGenBuffers(1, mVboIds, 0);
		mIndicesVBO = mVboIds[0];

		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndicesVBO);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, len * (Short.SIZE / 8),
				mShortBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	static Layer draw(Layer layer, float scale, float[] projection,
			float matrix[], int offset) {
		GLES20.glUseProgram(mTextureProgram);
		GlUtils.checkGlError("draw texture1");

		int va = hTextureTexCoord;
		if (!GLRenderer.vertexArray[va]) {
			GLES20.glEnableVertexAttribArray(va);
			GLRenderer.vertexArray[va] = true;
		}

		va = hTextureVertex;
		if (!GLRenderer.vertexArray[va]) {
			GLES20.glEnableVertexAttribArray(va);
			GLRenderer.vertexArray[va] = true;
		}

		TextureLayer tl = (TextureLayer) layer;
		GlUtils.checkGlError("draw texture2.");
		GLES20.glUniform1f(hTextureScale, scale);
		GLES20.glUniform1f(hTextureScreenScale, 1f / GLRenderer.mWidth);

		GLES20.glUniformMatrix4fv(hTextureProjMatrix, 1, false, projection, 0);
		GLES20.glUniformMatrix4fv(hTextureMVMatrix, 1, false, matrix, 0);
		GlUtils.checkGlError("draw texture2");

		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndicesVBO);
		GlUtils.checkGlError("draw texture3");

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tl.textures.id);
		GlUtils.checkGlError("draw texture4");

		GlUtils.checkGlError("draw texture5");
		GLES20.glVertexAttribPointer(hTextureVertex, 4,
				GLES20.GL_SHORT, false, 12, offset);
		GlUtils.checkGlError("draw texture..");

		GLES20.glVertexAttribPointer(hTextureTexCoord, 2,
				GLES20.GL_SHORT, false, 12, offset + 8);
		GlUtils.checkGlError("draw texture...");

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, (tl.verticesCnt / 4)
				* INDICES_PER_SPRITE, GLES20.GL_UNSIGNED_SHORT, 0);

		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		GlUtils.checkGlError("draw texture");

		return layer.next;
	}
}
