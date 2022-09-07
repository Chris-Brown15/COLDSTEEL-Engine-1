
out vec4 fragColor;										out vec4 FragColor;

in vec3 ourColor;										in vec3 ourColor;
in vec2 TexCoord;										in vec2 TexCoord;

uniform sampler2D ourTexture;							uniform sampler2D ourTexture;

void main()												void main()
{														{
    fragColor = texture(ourTexture, TexCoord);			FragColor = texture(ourTexture, TexCoord);



 * vec4(ourColor , 1.0)

new version below

#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoord;

uniform mat4 uTransform;

uniform mat4 uProjection;
uniform mat4 uView;

out vec4 ourColor;
out vec2 TexCoord;

void main()
{
    ourColor = aColor;
    TexCoord = aTexCoord;
    gl_Position = (uProjection * uView) * (vec4(aPos, 1.0));

}

#type fragment
#version 460 core
out vec4 FragColor;

in vec3 ourColor;
in vec2 TexCoord;

uniform sampler2D texture1;
uniform sampler2D texture2;

void main()
{
    FragColor = mix(texture(texture1, TexCoord) , texture(texture2, TexCoord) , 0.0);
}




old versions below


#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec3 aColor;
layout (location=2) in vec2 aTexCoord;

out vec3 ourColor;
out vec2 TexCoord;

void main()
{
    gl_Position = vec4(aPos, 1.0);
    ourColor = aColor;
    TexCoord = aTexCoord;
}

#type fragment
#version 460 core
out vec4 FragColor;

in vec3 ourColor;
in vec2 TexCoord;

uniform sampler2D texture1;
uniform sampler2D texture2;

void main()
{
    FragColor = mix(texture(texture1, TexCoord) , -texture(texture2, TexCoord) , 0.2);
}




#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec3 aColor;
layout (location=2) in vec2 aTexCoord;

out vec3 ourColor;
out vec2 TexCoord;

void main()
{
    gl_Position = vec4(aPos, 1.0);
    ourColor = aColor;
    TexCoord = aTexCoord;
}

#type fragment
#version 460 core
out vec4 FragColor;

in vec3 ourColor;
in vec2 TexCoord;

uniform sampler2D ourTexture;

void main()
{
    FragColor = texture(ourTexture, TexCoord);
}