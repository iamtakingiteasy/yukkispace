#version 120

struct light_t {
  bool enabled; // if light is enabled
  mat4 MVP;

  // attenuation
  vec3 intensity;

  float fade_const;
  float fade_linear;
  float fade_quadratic;

  float spot_exp;
  float spot_cut;

  // colors
  vec3 ambient;
  vec3 diffuse;
  vec3 specular;

  // geometry
  vec3 direction;
  vec4 position;
};

const int maximum_lights = 8;

// geometry
varying vec3  trans_normal;
varying vec3  trans_position;
varying vec4  trans_shadowuv[maximum_lights];

// material colors
varying vec3  trans_ambient;
varying vec3  trans_diffuse;
varying vec3  trans_specular;
varying float trans_shininess;

uniform mat4 M;
uniform mat4 V;

uniform sampler2D tex_depthmap1;
uniform sampler2D tex_depthmap2;
uniform sampler2D tex_depthmap3;
uniform sampler2D tex_depthmap4;
uniform sampler2D tex_depthmap5;
uniform sampler2D tex_depthmap6;
uniform sampler2D tex_depthmap7;
uniform sampler2D tex_depthmap8;

uniform light_t lights[maximum_lights];

float radians(float degrees) {
  return degrees * 3.14159 / 180.0;
}

vec3 ads_model(vec3 tnorm, int i) {
  vec2 coords;

  // direction from vertex to light position
  vec3 s = normalize(vec3(lights[i].position) - trans_position);

  // ambient compoennt
  vec3 ambient = lights[i].intensity * lights[i].ambient * trans_ambient;

  float angle  = acos(dot(-s, lights[i].direction));
  float cutoff = radians(lights[i].spot_cut/2.0);

  // spotlight
  if (angle < cutoff) {
    float spotFactor = pow(dot(-s, lights[i].direction), lights[i].spot_exp);
    vec3 v = normalize(-trans_position);
    vec3 h = normalize(v+s);
    vec3 diffuse  = lights[i].diffuse  * trans_diffuse  * max(0.0, dot(s, tnorm));
    vec3 specular = lights[i].specular * trans_specular * pow(max(0.0, dot(h, tnorm)), trans_shininess);
    return ambient + lights[i].intensity * spotFactor * (diffuse + specular);
  } else {
    return ambient;
  }
}

void main(void) {
  vec3 tnorm;

  if (gl_FrontFacing) {
    tnorm = trans_normal;
  } else {
    tnorm = -trans_normal;
  }

  vec3 color = vec3(0.0);

  for (int i = 0; i < maximum_lights; i++) {
    if (lights[i].enabled) {
      float factor = 1.0;
      vec4 shadow = texture2DProj(tex_depthmap1, trans_shadowuv[i].xyw);
      if (shadow.z < (trans_shadowuv[i].z-0.05)/trans_shadowuv[i].w) factor = 0.2;
      color += factor * ads_model(tnorm, i);
    }
  }



  /*
  if (i < 4) {
    if (i < 2) {
      if (i == 0) {
        depthmap = tex_depthmap1;
      } else {
        depthmap = tex_depthmap2;
      }
    } else {
      if (i == 2) {
        depthmap = tex_depthmap3;
      } else {
        depthmap = tex_depthmap4;
      }
    }
  } else {
    if (i < 6) {
      if (i == 4) {
        depthmap = tex_depthmap5;
      } else {
        depthmap = tex_depthmap6;
      }
    } else {
      if (i == 6) {
        depthmap = tex_depthmap7;
      } else {
        depthmap = tex_depthmap8;
      }
    }
  }
  */

  gl_FragColor = vec4(color, 1.0);
}