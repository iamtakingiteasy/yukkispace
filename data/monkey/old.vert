vec3 result_light = vec3(0.0);

  vec3 tnorm = normalize(VI * normal);
  vec4 eyeCoords = V * M * pos;
  vec3 v = normalize(-eyeCoords.xyz);

  int i = 0;

  vec3 light_dir = normalize((V * lights[i].M * vec4(lights[i].direction, 0)).xyz);
  vec4 light_pos = vec4(0,0,0,1);
  vec3 vertexToLight = (light_pos - eyeCoords).xyz;
  float distance = length(vertexToLight);
  float fadeFactor = lights[i].fade_const
                   + lights[i].fade_linear * distance
                   + lights[i].fade_quadratic * distance * distance;

  float fade = 1.0 / fadeFactor;

  if (lights[i].spot_cut < 90.0) {
    float clamped_cos = max(0.0, dot(normalize(vertexToLight), light_dir));
    if (clamped_cos < cos(deg2rad(lights[i].spot_cut))) {
  //    fade = 0.0;
    } else {
    //  fade *= pow(clamped_cos, lights[i].spot_exp);
    }
  }



  vec3 res_ambient  = /* light's ambient */ 1.0 * ambient;



  vec3 s = normalize((lights[i].position - eyeCoords).xyz);
  vec3 r = reflect(-s, tnorm);
  float sDotN =  max(0.0, dot(s, tnorm));


  vec3 res_diffuse  = fade * lights[i].diffuse * diffuse * sDotN;
  vec3 res_specular = vec3(0.0);

  if (sDotN > 0.0)  {
    res_specular = lights[i].specular * specular * pow(max(0.0, dot(r,v)), shininess);
  }



  result_light += res_ambient + res_diffuse + res_specular;


  /*
  int i = 0;

  vec4 light_pos = vec4(0,0,0,1);
  vec3 normal_dir = normalize(VI * normal);
  vec3 light_dir = -normalize((V * lights[i].M * vec4(0,1,0,0)).xyz);
  float fade = 1.0;

  if (light_pos.w == 0) {
  } else {
    vec3 vertex_to_light = (light_pos - V * M * pos).xyz;
    float distance = length(vertex_to_light);
    float fade_factor = lights[i].fade_const
                      + lights[i].fade_linear * distance
                      + lights[i].fade_quadratic * distance * distance;

    fade = 1.0 / fade_factor;

    if (lights[i].spot_cut <= 90.0) {
      float clamped_cos = max(0.0, dot(normalize(vertex_to_light), light_dir));
      if (clamped_cos < cos(lights[i].spot_cut * 3.14159 / 180.0)) {
        fade = 0.0;
      } else {
        fade *= pow(clamped_cos, lights[i].spot_exp);
      }
    }
  }

  vec3 diffuse_reflection = fade
                          * lights[i].diffuse.rgb
                          * diffuse.rgb
                          * max(1.0, min(1.0, max(0.0, dot(normal_dir, light_dir)) * 10) * 5);

  result_light += diffuse_reflection;

  */