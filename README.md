# Region Plugin

A Minecraft server plugin that provides region protection functionality. Players can create protected areas with customizable permissions and access control.

## Features

- Region creation using selection wand
- Player whitelist management
- Configurable protection flags
- GUI-based region management
- MySQL database support
- Visual feedback with particles
- Entry/exit notifications
- Extensible flag system

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/region` | Opens region management menu | `region.menu` |
| `/region create <name>` | Creates a region at selected location | `region.create` |
| `/region wand` | Gives selection wand | `region.create` |
| `/region add <name> <username>` | Adds player to whitelist | `region.add` |
| `/region remove <name> <username>` | Removes player from whitelist | `region.remove` |
| `/region whitelist <name>` | Lists whitelisted players | `region.whitelist` |
| `/region flag <name> <flag> <state>` | Sets region flag | `region.flag` |
| `/region list` | Shows all your regions | `region.menu` |
| `/region rename <old> <new>` | Renames a region | `region.rename` |
| `/region redefine <name>` | Redefines region boundaries | `region.redefine` |
| `/region delete <name>` | Deletes a region | `region.delete` |

## GUI System

### Regions Menu
- Displays all owned regions
- Click to access region management
- Shows owner and member information

### Region Management Menu
- Rename region
- Manage whitelist
- Redefine boundaries
- Configure flags
- Delete region

### Whitelist Management
- View all whitelisted players
- Remove players with one click
- Real-time updates

### Flag Configuration
- Block break protection
- Block place protection
- Interaction protection
- Entity damage protection
- Three states: Everyone, Whitelist, None

## Protection Flags

Flags control what actions are allowed in regions:

- **Everyone**: All players can perform the action
- **Whitelist**: Only whitelisted players can perform the action
- **None**: No one can perform the action

### Available Flags
- `block-break` - Block breaking protection
- `block-place` - Block placing protection
- `interact` - Block interaction protection
- `entity-damage` - Entity damage protection

### Custom Flags
```java
RegionFlags.IFlag customFlag = RegionFlags.CustomFlagRegistry.registerCustomFlag("my-flag");
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `region.menu` | Access region menus | `true` |
| `region.create` | Create regions | `op` |
| `region.add` | Add players to whitelist | `op` |
| `region.remove` | Remove players from whitelist | `op` |
| `region.whitelist` | View whitelist | `true` |
| `region.flag` | Edit region flags | `op` |
| `region.bypass` | Bypass all region protection | `op` |
| `region.rename` | Rename regions | `op` |
| `region.redefine` | Redefine region boundaries | `op` |
| `region.delete` | Delete regions | `op` |

## Configuration

### Database Settings
```yaml
storage:
  type: "mysql"

mysql:
  host: "localhost"
  port: 3306
  database: "regions"
  user: "root"
  password: "your_password"
  pool:
    max-connections: 10
    connection-timeout: 30000
    idle-timeout: 600000
```

### Feature Settings
```yaml
features:
  show-particles: true
  show-entry-title: true
  show-boundaries: false
```

## Installation

1. Download the latest release JAR file
2. Place it in your server's `plugins/` folder
3. Start or restart your server
4. Configure database settings in `plugins/Region/config.yml`
5. Set up permissions for your players

## Dependencies

- Paper/Spigot 1.21+
- SmartInventory (GUI library)
- HikariCP (Connection pooling)
- MySQL Connector (Database driver)

## Building from Source

```bash
git clone https://github.com/yourusername/Region.git
cd Region
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## Database Schema

### regions
- `name` (VARCHAR) - Primary key
- `owner` (VARCHAR) - Player UUID
- `world` (VARCHAR) - World name
- `corner1_x/y/z` (INT) - First corner coordinates
- `corner2_x/y/z` (INT) - Second corner coordinates
- `created_at` (BIGINT) - Creation timestamp

### region_whitelist
- `region_name` (VARCHAR) - Foreign key to regions
- `player_uuid` (VARCHAR) - Player UUID

### region_flags
- `region_name` (VARCHAR) - Foreign key to regions
- `flag_name` (VARCHAR) - Flag identifier
- `flag_state` (VARCHAR) - Flag state

## API Usage

### Custom Flags
```java
RegionFlags.IFlag customFlag = RegionFlags.CustomFlagRegistry.registerCustomFlag("my-flag");
```

### Permission Checking
```java
RegionPlugin plugin = RegionPlugin.getInstance();
RegionManager manager = plugin.getRegionManager();

boolean canBreak = manager.hasPermission(player, location, RegionFlags.Flag.BLOCK_BREAK);
```

## GitHub Setup

### Initial Setup
1. Create a new repository on GitHub
2. Clone this repository locally
3. Add your GitHub repository as remote origin
4. Push the code to GitHub

```bash
git remote add origin https://github.com/yourusername/Region.git
git branch -M main
git push -u origin main
```

### Release Process
1. Update version in `build.gradle` and `plugin.yml`
2. Create a new tag
3. Build the project
4. Upload the JAR to GitHub Releases

```bash
git tag v1.0.0
git push origin v1.0.0
./gradlew build
```

## Support

- Report issues on GitHub Issues
- Check the wiki for detailed guides

## License

MIT License - see LICENSE file for details.

## Developer

- **alexetrey** - Plugin developer

## Libraries Used

- SmartInventory by MinusKube
- HikariCP by Brett Wooldridge 