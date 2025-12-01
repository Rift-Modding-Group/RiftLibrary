package anightdazingzoroark.riftlib.jsonParsing.constructor;

import anightdazingzoroark.riftlib.core.ConstantValue;
import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticle;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class ParticleConstructor {
    public static ParticleBuilder createParticleBuilder(MolangParser parser, String namespace, RawParticle rawParticle) throws NumberFormatException, MolangException {
        ParticleBuilder toReturn = new ParticleBuilder();

        //get name
        toReturn.identifier = rawParticle.rawParticleEffect.description.identifier;

        //get texture
        String textureLocation = rawParticle.rawParticleEffect.description.basicRenderParameters.texture;
        if (textureLocation.startsWith("textures/")) {
            textureLocation = textureLocation.substring("textures/".length()); // "particle/particles"
        }
        if (textureLocation.endsWith(".png")) {
            textureLocation = textureLocation.substring(0, textureLocation.length() - 4);
        }

        toReturn.texture = new ResourceLocation(namespace, textureLocation);

        //get material
        toReturn.material = ParticleMaterial.getMaterialFromString(rawParticle.rawParticleEffect.description.basicRenderParameters.material);

        //get components
        Map<String, RawParticleComponent> particleComponents = rawParticle.rawParticleEffect.components;
        for (Map.Entry<String, RawParticleComponent> rawComponent : particleComponents.entrySet()) {
            //particle appearance, its the one that gets the uv of the texture
            if (rawComponent.getKey().equals("minecraft:particle_appearance_billboard")) {
                //specifies the x/y size of the billboard
                //evaluated every frame
                if (rawComponent.getValue().componentValues.containsKey("size")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("size");
                    toReturn.size = parseExpressionArray(parser, 2, componentValue);
                }
                //get UV information
                if (rawComponent.getValue().componentValues.containsKey("uv")) {
                    Map<String, RawParticleComponent.ComponentValue> uvComponents = rawComponent.getValue().componentValues.get("uv").object;

                    //uv components
                    for (Map.Entry<String, RawParticleComponent.ComponentValue> uvComponent : uvComponents.entrySet()) {
                        if (uvComponent.getKey().equals("texture_width")) {
                            toReturn.textureWidth = (int) uvComponent.getValue().number.floatValue();
                        }
                        if (uvComponent.getKey().equals("texture_height")) {
                            toReturn.textureHeight = (int) uvComponent.getValue().number.floatValue();
                        }
                        if (uvComponent.getKey().equals("uv")) {
                            toReturn.uv = parseExpressionArray(parser, 2, uvComponent.getValue());
                        }
                        if (uvComponent.getKey().equals("uv_size")) {
                            toReturn.uvSize = parseExpressionArray(parser, 2, uvComponent.getValue());
                        }
                    }
                }
            }
            //emitter lifetime, (emitters create and spawn the particles)
            if (rawComponent.getKey().equals("minecraft:emitter_lifetime_expression")) {
                //when the expression is non-zero, the emitter will emit particles.
                //evaluated every frame
                if (rawComponent.getValue().componentValues.containsKey("activation_expression")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("activation_expression");
                    toReturn.emitterActivationValue = parseExpression(parser, componentValue);
                }
                //will expire if the expression is non-zero.
                //evaluated every frame
                if (rawComponent.getValue().componentValues.containsKey("expiration_expression")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("expiration_expression");
                    toReturn.emitterExpirationValue = parseExpression(parser, componentValue);
                }
            }
            //-----emitter rate component stuff starts here-----
            //-----(emitter rate is rate at which emitter creates particles)-----
            if (rawComponent.getKey().equals("minecraft:emitter_rate_instant")) {
                ParticleBuilder.InstantEmitterRate instantEmitterRate = new ParticleBuilder.InstantEmitterRate();
                if (rawComponent.getValue().componentValues.containsKey("num_particles")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("num_particles");
                    instantEmitterRate.particleCount = parseExpression(parser, componentValue);
                }
                toReturn.emitterRate = instantEmitterRate;
            }
            //-----emitter rate component stuff ends here-----
            //particle lifetime
            if (rawComponent.getKey().equals("minecraft:particle_lifetime_expression")) {
                //this expression makes the particle expire when true (non-zero)
                //evaluated once per particle
                //evaluated every frame
                if (rawComponent.getValue().componentValues.containsKey("expiration_expression")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("expiration_expression");
                    toReturn.particleExpirationValue = parseExpression(parser, componentValue);
                }
                //particle will expire after this much time, evaluated once
                if (rawComponent.getValue().componentValues.containsKey("max_lifetime")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("max_lifetime");
                    toReturn.particleLifetimeValue = parseExpression(parser, componentValue);
                }
            }
            //-----emitter shape component stuff starts here-----
            if (rawComponent.getKey().equals("minecraft:emitter_shape_sphere")) {
                ParticleBuilder.SphereEmitterShape sphereEmitterShape = new ParticleBuilder.SphereEmitterShape();

                if (rawComponent.getValue().componentValues.containsKey("offset")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
                    sphereEmitterShape.offset = parseExpressionArray(parser, 3, componentValue);
                }
                if (rawComponent.getValue().componentValues.containsKey("radius")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("radius");
                    sphereEmitterShape.radius = parseExpression(parser, componentValue);
                }
                if (rawComponent.getValue().componentValues.containsKey("surface_only")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("surface_only");
                    sphereEmitterShape.surfaceOnly = componentValue.bool == Boolean.TRUE;
                }
                if (rawComponent.getValue().componentValues.containsKey("direction")) {
                    RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("direction");
                    //direction as string
                    if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) {
                        if (componentValue.string.equals("inwards") || componentValue.string.equals("outwards")) {
                            sphereEmitterShape.particleDirection = componentValue.string;
                        }
                        else throw new InvalidValueException("Invalid value "+componentValue.string+" for direction!");
                    }
                    //custom direction
                    else if (componentValue.valueType == RawParticleComponent.ComponentValueType.ARRAY) {
                        sphereEmitterShape.customParticleDirection = parseExpressionArray(parser, 3, componentValue);
                    }
                }

                toReturn.emitterShape = sphereEmitterShape;
            }
            //-----emitter shape component stuff ends here-----
        }

        return toReturn;
    }

    private static IValue parseExpression(MolangParser parser, RawParticleComponent.ComponentValue componentValue) throws MolangException {
        //component value was a string
        if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) return parser.parseExpression(componentValue.string);
        //component value was a double
        else return ConstantValue.fromDouble(componentValue.number);
    }

    private static IValue[] parseExpressionArray(MolangParser parser, int intendedSize, RawParticleComponent.ComponentValue componentValue) throws MolangException {
        if (componentValue.valueType == RawParticleComponent.ComponentValueType.ARRAY) {
            if (componentValue.array.size() != intendedSize) throw new InvalidValueException("Invalid array length!");

            IValue[] toReturn = new IValue[componentValue.array.size()];
            for (int i = 0; i < toReturn.length; i++) {
                RawParticleComponent.ComponentValue value = componentValue.array.get(i);
                toReturn[i] = parseExpression(parser, value);
            }
            return toReturn;
        }
        else throw new InvalidValueException("Component was not an array!");
    }
}
