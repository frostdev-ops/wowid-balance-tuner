# WoWID Balance Tuner

WoWID Balance Tuner is a Minecraft mod that allows server administrators to manage and balance various player abilities and mob attributes, with a focus on dimension-specific modifications.

## Features

- Restrict flight of any kind in any specified dimension.
- Give any item the ability to grant creative flight.
- Modify the attributes of mobs based on their dimension.
- Dynamic entity blacklisting to improve performance over time.

## Installation
# TBA

## Configuration

The mod uses a JSON configuration file located at `config/wowidbt.json`. Ensure the file is properly formatted and contains the necessary settings for your server.

### Example Configuration

Here is an example of how to configure `wowidbt.json`:

```json
{
  "global_settings": {
     "debug": false, 
     "creative_flight": ["minecraft:diamond_chestplate"], 
     "Flight_Disabled_dims": ["minecraft:the_nether"], 
     "global_overrides": {
        "minecraft:zombie": {
          "attributes": {
            "generic.maxHealth": 30.0
          },
          "variance": {
            "generic.maxHealth": 3.0
          }
        }
     },
     "global_variance": {
        "generic.maxHealth": 2.0
     }
  },
  "dimensions": {
    "minecraft:overworld": {
      "attributes": {
        "generic.maxHealth": 40.0,
        "generic.movementSpeed": 0.3
      },
       "variance": {
          "generic.maxHealth": 5.0
       }, 
       "overrides": {
        "minecraft:zombie": {
          "attributes": {
            "generic.attackDamage": 10.0
          },
          "variance": {
            "generic.attackDamage": 2.0
          }
        }
      }
    },
     "minecraft:the_end": {
        "tier": 1,
        "overrides": {
          "minecraft:ender_dragon": {
            "attributes": {
              "generic.max_health": 9001.0
            },
            "variance": {
              "generic.max_health": 69.0
            }
          }
        }
     }
  },
  "tiers": {
    "1": {
      "attributes": {
        "generic.maxHealth": 50.0
      },
       "variance": {
          "generic.maxHealth": 10.0
       },
      "overrides": {
        "minecraft:skeleton": {
          "attributes": {
            "generic.attackDamage": 15.0
          },
          "variance": {
            "generic.attackDamage": 3.0
          }
        }
      }
    }
  }
}
```

### Explanation

- **global_settings**: Contains global settings for the mod.
   - **debug**: Enables or disables debug logging.
   - **creative_flight**: List of items that grant creative flight.
   - **Flight_Disabled_dims**: List of dimensions where flight is disabled.
   - **global_overrides**: Global overrides for specific entities across all dimensions.
   - **global_variance**: Global variance values for attributes across all dimensions.
- **dimensions**: Contains dimension-specific settings.
   - **attributes**: Default attributes for entities in the dimension.
   - **overrides**: Specific overrides for certain entities in the dimension.
   - **variance**: Variance values for attributes in the dimension.
- **tiers**: Contains tier-specific settings.
   - **tier**: Tiers a container for modifications that can be applied to multiple dimensions.
      - tier use is intended for servers with a lot of dimensions where you want to apply the same modifications to multiple dimensions.
   - **attributes**: Default attributes for entities in the tier.
   - **overrides**: Specific overrides for certain entities in the tier.
   - **variance**: Variance values for attributes in the tier.

## Usage
- **pirority**: The mod will prioritize the most specific configuration for an entity. For example, if an entity has a global override and a dimension override, the dimension override will take precedence.
- **overrides**: Overrides are used to apply specific modifications to certain entities. For example, if you want to increase the max health of zombies in the overworld, you can create an override for zombies in the overworld.
- **attributes**: Attributes are the base values for entities. For example, if you want all entities in the overworld to have a max health of 40, you can set the max health attribute to 40 in the overworld configuration.
- **variance**: Variance values are used to add randomness to the attributes of entities. For example, if an entity has a max health of 40 and a variance of 5, the entity's max health will be between 35 and 45.
- **tier**: Tiers are used to apply the same modifications to multiple dimensions. For example, if you have multiple dimensions with the same mob attributes, you can create a tier with those attributes and apply it to all the dimensions.
- **blacklist**: The mod will automatically blacklist entities that are causing performance issues because you are trying to apply an attribute to an entity that does not have that attribute. I don't recommend manually adding to the blacklist file located at `config/wowidbt/blacklist.json`, the mod will handle this for you.
- **creative_flight**: Creative flight is granted by holding an item in your hand, off-hand, or having the defined item equipped. You can specify which items grant creative flight by adding their item IDs to the creative_flight list in the global settings.
- **Flight_Disabled_dims**: Flight is disabled in dimensions specified in the Flight_Disabled_dims list in the global settings. This is achieved by varius checks and methods. Disclaimer: This is not a foolproof method and while it likely cannot be bypassed, it is very aggressive with what it considers "flight". That being said it is quite effective at preventing flight in the specified dimensions. 
- **debug**: Debug mode can be enabled in the global settings. This will log additional information to the console to help with troubleshooting.


## Contributing

1. Fork the repository.
2. Create a new branch:
    ```sh
    git checkout -b feature/your-feature-name
    ```
3. Make your changes and commit them:
    ```sh
    git commit -m "Add your feature"
    ```
4. Push to the branch:
    ```sh
    git push origin feature/your-feature-name
    ```
5. Open a pull request.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](https://www.gnu.org/licenses/gpl-3.0.md) file for details.

## Contact

For any questions or issues, please open an issue on GitHub or contact the project maintainer.