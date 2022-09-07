#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoord;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 rotation;
uniform mat4 translation;

float xAlphaOffset;
float yAlphaOffset;

out vec4 Color;
out vec2 TexCoord;

void main() {

    Color = aColor;
   	TexCoord = aTexCoord;
   	gl_Position = ((uProjection * uView * translation) * rotation * (vec4(aPos, 1.0)));

}

#type fragment
#version 460 core
out vec4 FragColor1;

uniform sampler2D TEX_SAMPLER;

in vec4 Color;
in vec2 TexCoord;

uniform vec3 removed;
uniform vec3 RGBAdd;
uniform int mode;

void main() {

	vec4 thisTexture = texture(TEX_SAMPLER , TexCoord);
	vec3 thisOpaqueTexture = thisTexture.xyz;

	//textured
	if(mode == 1){

		if (thisOpaqueTexture == removed) discard;
		
		vec3 filtered = vec3(thisOpaqueTexture.x + RGBAdd.x , thisOpaqueTexture.y + RGBAdd.y , thisOpaqueTexture.z + RGBAdd.z);
		FragColor1 = vec4(filtered , Color.a);

	//blending two colors
	} else if (mode == 2){

		FragColor1 = Color * thisTexture;

	//single color
	} else if (mode == 0) {

		FragColor1 = Color;

	}

}
