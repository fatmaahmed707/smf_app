import 'package:flutter/material.dart';

import '../../models/role_summary.dart';
import '../../models/user.dart';
import '../../services/api_service.dart';
import '../../services/roles_service.dart';
import '../../services/users_service.dart';

class UsersManagementPage extends StatefulWidget {
  const UsersManagementPage({super.key});

  @override
  State<UsersManagementPage> createState() => _UsersManagementPageState();
}

class _UsersManagementPageState extends State<UsersManagementPage> {
  final UsersService _usersService = UsersService();
  final RolesService _rolesService = RolesService();

  bool _isLoading = true;
  List<User> _users = const [];
  List<RoleSummary> _roles = const [];
  String? _errorMessage;
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final results = await Future.wait([
        _usersService.getUsers(),
        _rolesService.getRoles(),
      ]);

      if (!mounted) {
        return;
      }

      setState(() {
        _users = results[0] as List<User>;
        _roles = results[1] as List<RoleSummary>;
        _isLoading = false;
      });
    } on ApiException catch (error) {
      if (!mounted) {
        return;
      }
      setState(() {
        _errorMessage = error.message;
        _isLoading = false;
      });
    } catch (_) {
      if (!mounted) {
        return;
      }
      setState(() {
        _errorMessage = 'Failed to load users.';
        _isLoading = false;
      });
    }
  }

  Future<void> _showCreateDialog() async {
    await _showUserFormDialog(
      context,
      roles: _roles,
      title: 'Create User',
      submitLabel: 'Create',
      onSubmit: ({
        required String username,
        required String email,
        required String password,
        required String roleName,
      }) async {
        await _usersService.createUser(
          username: username,
          email: email,
          password: password,
          roles: {roleName},
        );
      },
    );

    await _load();
  }

  Future<void> _showEditDialog(User user) async {
    await _showUserFormDialog(
      context,
      roles: _roles,
      title: 'Update User',
      submitLabel: 'Save',
      initialUsername: user.name,
      initialEmail: user.email,
      initialRole: user.role,
      onSubmit: ({
        required String username,
        required String email,
        required String password,
        required String roleName,
      }) async {
        await _usersService.updateUser(
          id: user.id,
          username: username,
          email: email,
          password: password.isEmpty ? null : password,
          roles: {roleName},
        );
      },
    );

    await _load();
  }

  Future<void> _deleteUser(User user) async {
    final confirmed = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Delete user?'),
            content: Text('This will delete ${user.name}.'),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('Cancel'),
              ),
              FilledButton(
                onPressed: () => Navigator.pop(context, true),
                child: const Text('Delete'),
              ),
            ],
          ),
        ) ??
        false;

    if (!confirmed) {
      return;
    }

    try {
      await _usersService.deleteUser(user.id);
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('User deleted successfully.')),
      );
      await _load();
    } on ApiException catch (error) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(error.message)),
      );
    }
  }

  Future<void> _showDetails(User user) async {
    try {
      final details = await _usersService.getUser(user.id);
      if (!mounted) {
        return;
      }
      await showDialog<void>(
        context: context,
        builder: (context) => AlertDialog(
          title: Text(details.name),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('ID: ${details.id}'),
              Text('Email: ${details.email}'),
              Text('Role: ${details.role ?? '-'}'),
              Text('Provider: ${details.provider ?? '-'}'),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Close'),
            ),
          ],
        ),
      );
    } on ApiException catch (error) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(error.message)),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final filteredUsers = _users.where((user) {
      final query = _searchQuery.trim().toLowerCase();
      if (query.isEmpty) return true;
      return user.name.toLowerCase().contains(query) ||
          user.email.toLowerCase().contains(query) ||
          (user.role ?? '').toLowerCase().contains(query);
    }).toList();

    return _UsersConsole(
      users: filteredUsers,
      totalUsers: _users.length,
      isLoading: _isLoading,
      errorMessage: _errorMessage,
      searchQuery: _searchQuery,
      onSearchChanged: (value) => setState(() => _searchQuery = value),
      onRefresh: _isLoading ? null : _load,
      onAdd: _isLoading ? null : _showCreateDialog,
      onDetails: _showDetails,
      onEdit: _showEditDialog,
      onDelete: _deleteUser,
    );
  }
}

Future<void> _showUserFormDialog(
  BuildContext context, {
  required List<RoleSummary> roles,
  required String title,
  required String submitLabel,
  required Future<void> Function({
    required String username,
    required String email,
    required String password,
    required String roleName,
  }) onSubmit,
  String? initialUsername,
  String? initialEmail,
  String? initialRole,
}) async {
  final usernameController = TextEditingController(text: initialUsername ?? '');
  final emailController = TextEditingController(text: initialEmail ?? '');
  final passwordController = TextEditingController();
  String selectedRole =
      initialRole ?? (roles.isNotEmpty ? roles.first.roleName : 'ROLE_USER');
  final formKey = GlobalKey<FormState>();

  await showDialog<void>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setDialogState) => AlertDialog(
        title: Text(title),
        content: SizedBox(
          width: 420,
          child: Form(
            key: formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: usernameController,
                  decoration: const InputDecoration(
                    labelText: 'Username',
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) => value == null || value.trim().isEmpty
                      ? 'Username is required.'
                      : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: emailController,
                  decoration: const InputDecoration(
                    labelText: 'Email',
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) => value == null || !value.contains('@')
                      ? 'Enter a valid email.'
                      : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: passwordController,
                  decoration: InputDecoration(
                    labelText: initialUsername == null
                        ? 'Password'
                        : 'Password (optional)',
                    border: const OutlineInputBorder(),
                  ),
                  validator: (value) {
                    if (initialUsername == null &&
                        (value == null || value.isEmpty)) {
                      return 'Password is required.';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  initialValue: selectedRole,
                  items: roles
                      .map(
                        (role) => DropdownMenuItem<String>(
                          value: role.roleName,
                          child: Text(role.roleName),
                        ),
                      )
                      .toList(),
                  onChanged: (value) {
                    if (value != null) {
                      setDialogState(() {
                        selectedRole = value;
                      });
                    }
                  },
                  decoration: const InputDecoration(
                    labelText: 'Role',
                    border: OutlineInputBorder(),
                  ),
                ),
              ],
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () async {
              if (!formKey.currentState!.validate()) {
                return;
              }

              await onSubmit(
                username: usernameController.text.trim(),
                email: emailController.text.trim(),
                password: passwordController.text,
                roleName: selectedRole,
              );

              if (context.mounted) {
                Navigator.pop(context);
              }
            },
            child: Text(submitLabel),
          ),
        ],
      ),
    ),
  );

  usernameController.dispose();
  emailController.dispose();
  passwordController.dispose();
}

class _UsersConsole extends StatelessWidget {
  final List<User> users;
  final int totalUsers;
  final bool isLoading;
  final String? errorMessage;
  final String searchQuery;
  final ValueChanged<String> onSearchChanged;
  final VoidCallback? onRefresh;
  final VoidCallback? onAdd;
  final ValueChanged<User> onDetails;
  final ValueChanged<User> onEdit;
  final ValueChanged<User> onDelete;

  const _UsersConsole({
    required this.users,
    required this.totalUsers,
    required this.isLoading,
    required this.errorMessage,
    required this.searchQuery,
    required this.onSearchChanged,
    required this.onRefresh,
    required this.onAdd,
    required this.onDetails,
    required this.onEdit,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final palette = _UsersPalette.of(context);
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 14, 20, 20),
      child: Container(
        decoration: BoxDecoration(
          color: palette.panel,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: palette.border),
          boxShadow: [
            BoxShadow(
              color: palette.shadow,
              blurRadius: 28,
              offset: const Offset(0, 16),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _UsersToolbar(
                palette: palette,
                searchQuery: searchQuery,
                onSearchChanged: onSearchChanged,
                onRefresh: onRefresh,
                onAdd: onAdd,
              ),
              const SizedBox(height: 12),
              Expanded(
                child: isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : errorMessage != null
                        ? _ErrorPanel(message: errorMessage!)
                        : _UsersTable(
                            palette: palette,
                            users: users,
                            onDetails: onDetails,
                            onEdit: onEdit,
                            onDelete: onDelete,
                          ),
              ),
              const SizedBox(height: 12),
              _UsersFooter(
                palette: palette,
                totalUsers: totalUsers,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _UsersToolbar extends StatelessWidget {
  final _UsersPalette palette;
  final String searchQuery;
  final ValueChanged<String> onSearchChanged;
  final VoidCallback? onRefresh;
  final VoidCallback? onAdd;

  const _UsersToolbar({
    required this.palette,
    required this.searchQuery,
    required this.onSearchChanged,
    required this.onRefresh,
    required this.onAdd,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final compact = constraints.maxWidth < 760;
        final searchField = SizedBox(
          width: compact ? double.infinity : 260,
          height: 42,
          child: TextField(
            onChanged: onSearchChanged,
            style: TextStyle(color: palette.textPrimary),
            decoration: InputDecoration(
              prefixIcon: Icon(Icons.search_rounded,
                  color: palette.textMuted, size: 20),
              hintText: 'Search users...',
              hintStyle: TextStyle(color: palette.textMuted),
              filled: true,
              fillColor: palette.control,
              contentPadding:
                  const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide(color: palette.tableBorder),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide(color: palette.tableBorder),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide(color: palette.blue),
              ),
            ),
          ),
        );
        final actions = Wrap(
          spacing: 10,
          runSpacing: 10,
          children: [
            _IconSquareButton(
              palette: palette,
              icon: Icons.filter_alt_outlined,
              onPressed: () {},
            ),
            _IconSquareButton(
              palette: palette,
              icon: Icons.refresh_rounded,
              onPressed: onRefresh,
            ),
            FilledButton.icon(
              onPressed: onAdd,
              icon: const Icon(Icons.person_add_alt_1_rounded, size: 18),
              label: const Text('Add User'),
              style: FilledButton.styleFrom(
                backgroundColor: palette.blue,
                foregroundColor: Colors.white,
                padding:
                    const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
                textStyle: const TextStyle(fontWeight: FontWeight.w900),
              ),
            ),
          ],
        );

        if (compact) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Users Management',
                style: TextStyle(
                  color: palette.textPrimary,
                  fontSize: 20,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const SizedBox(height: 12),
              searchField,
              const SizedBox(height: 12),
              actions,
            ],
          );
        }

        return Row(
          children: [
            Expanded(
              child: Text(
                'Users Management',
                style: TextStyle(
                  color: palette.textPrimary,
                  fontSize: 20,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ),
            searchField,
            const SizedBox(width: 12),
            actions,
          ],
        );
      },
    );
  }
}

class _UsersTable extends StatelessWidget {
  final _UsersPalette palette;
  final List<User> users;
  final ValueChanged<User> onDetails;
  final ValueChanged<User> onEdit;
  final ValueChanged<User> onDelete;

  const _UsersTable({
    required this.palette,
    required this.users,
    required this.onDetails,
    required this.onEdit,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(10),
      child: Container(
        decoration: BoxDecoration(
          border: Border.all(color: palette.tableBorder),
          borderRadius: BorderRadius.circular(10),
        ),
        child: LayoutBuilder(
          builder: (context, constraints) {
            final tableWidth =
                constraints.maxWidth < 1260 ? 1260.0 : constraints.maxWidth;
            return SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: ConstrainedBox(
                constraints: BoxConstraints(minWidth: constraints.maxWidth),
                child: Center(
                  child: SizedBox(
                    width: tableWidth,
                    child: Column(
                      children: [
                        _UsersTableHeader(palette: palette),
                        Expanded(
                          child: users.isEmpty
                              ? Center(
                                  child: Text(
                                    'No users found.',
                                    style: TextStyle(
                                      color: palette.textMuted,
                                      fontWeight: FontWeight.w700,
                                    ),
                                  ),
                                )
                              : ListView.separated(
                                  itemCount: users.length,
                                  separatorBuilder: (_, __) => Divider(
                                    height: 1,
                                    color: palette.tableBorder,
                                  ),
                                  itemBuilder: (context, index) =>
                                      _UsersTableRow(
                                    palette: palette,
                                    user: users[index],
                                    index: index,
                                    onDetails: onDetails,
                                    onEdit: onEdit,
                                    onDelete: onDelete,
                                  ),
                                ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}

class _UsersTableHeader extends StatelessWidget {
  final _UsersPalette palette;

  const _UsersTableHeader({required this.palette});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 48,
      color: palette.header,
      padding: const EdgeInsets.symmetric(horizontal: 14),
      child: Row(
        children: [
          _UsersHeaderCell('USER', width: 260, palette: palette),
          _UsersHeaderCell('ROLE', width: 180, palette: palette),
          _UsersHeaderCell('STATUS', width: 140, palette: palette),
          _UsersHeaderCell('LAST ACTIVE', width: 210, palette: palette),
          _UsersHeaderCell('EMAIL', width: 260, palette: palette),
          _UsersHeaderCell('ACTIONS', width: 170, palette: palette),
        ],
      ),
    );
  }
}

class _UsersHeaderCell extends StatelessWidget {
  final String label;
  final double width;
  final _UsersPalette palette;

  const _UsersHeaderCell(
    this.label, {
    required this.width,
    required this.palette,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: width,
      child: Text(
        label,
        style: TextStyle(
          color: palette.headerText,
          fontSize: 12,
          fontWeight: FontWeight.w900,
        ),
      ),
    );
  }
}

class _UsersTableRow extends StatelessWidget {
  final _UsersPalette palette;
  final User user;
  final int index;
  final ValueChanged<User> onDetails;
  final ValueChanged<User> onEdit;
  final ValueChanged<User> onDelete;

  const _UsersTableRow({
    required this.palette,
    required this.user,
    required this.index,
    required this.onDetails,
    required this.onEdit,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final role = _normalizedRole(user);
    final accent = _roleColor(role);
    return Container(
      height: 58,
      color: palette.row,
      padding: const EdgeInsets.symmetric(horizontal: 14),
      child: Row(
        children: [
          SizedBox(
            width: 260,
            child: Row(
              children: [
                _UserAvatar(
                  letter: _avatarLetter(user, index),
                  color: accent,
                  palette: palette,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        user.name.isEmpty ? 'User' : user.name,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          color: palette.textPrimary,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        user.email,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          color: palette.textMuted,
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          SizedBox(
            width: 180,
            child: _RolePill(role: role, color: accent, palette: palette),
          ),
          SizedBox(
            width: 140,
            child: _StatusPill(palette: palette),
          ),
          SizedBox(
            width: 210,
            child: Row(
              children: [
                Icon(Icons.schedule_rounded,
                    size: 15, color: palette.textMuted),
                const SizedBox(width: 7),
                Text(
                  _lastActive(index),
                  style: TextStyle(
                    color: palette.textPrimary,
                    fontWeight: FontWeight.w700,
                    fontSize: 13,
                  ),
                ),
              ],
            ),
          ),
          SizedBox(
            width: 260,
            child: Text(
              user.email,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                color: palette.textPrimary,
                fontWeight: FontWeight.w600,
                fontSize: 13,
              ),
            ),
          ),
          SizedBox(
            width: 138,
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                _UserActionButton(
                  icon: Icons.visibility_outlined,
                  color: palette.blue,
                  palette: palette,
                  tooltip: 'Details',
                  onPressed: () => onDetails(user),
                ),
                const SizedBox(width: 8),
                _UserActionButton(
                  icon: Icons.edit_rounded,
                  color: palette.blue,
                  palette: palette,
                  tooltip: 'Edit',
                  onPressed: () => onEdit(user),
                ),
                const SizedBox(width: 8),
                _UserActionButton(
                  icon: Icons.delete_outline_rounded,
                  color: const Color(0xFFFF3B43),
                  palette: palette,
                  tooltip: 'Delete',
                  onPressed: () => onDelete(user),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  static String _normalizedRole(User user) {
    final value = user.role?.trim().isNotEmpty == true
        ? user.role!.trim()
        : user.roles.isNotEmpty
            ? user.roles.first.trim()
            : 'USER';
    return value.replaceAll('ROLE_', '').toUpperCase();
  }

  static String _avatarLetter(User user, int index) {
    final source = user.name.trim().isNotEmpty ? user.name.trim() : user.email;
    if (source.isEmpty) return String.fromCharCode(65 + index % 26);
    return source[0].toUpperCase();
  }

  static Color _roleColor(String role) {
    if (role.contains('ENGINEER')) return const Color(0xFF168BFF);
    if (role.contains('MANAGER')) return const Color(0xFF19D389);
    if (role.contains('WORKER')) return const Color(0xFFFFA51F);
    if (role.contains('ADMIN')) return const Color(0xFFFF3B43);
    if (role.contains('SUPERVISOR')) return const Color(0xFF8B5CF6);
    return const Color(0xFF168BFF);
  }

  static String _lastActive(int index) {
    const values = [
      'Today, 08:42 AM',
      'Today, 07:15 AM',
      'Yesterday, 11:32 PM',
      'Yesterday, 09:20 PM',
      'May 20, 2025, 04:10 PM',
    ];
    return values[index % values.length];
  }
}

class _UserAvatar extends StatelessWidget {
  final String letter;
  final Color color;
  final _UsersPalette palette;

  const _UserAvatar({
    required this.letter,
    required this.color,
    required this.palette,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 36,
      height: 36,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: color.withValues(alpha: palette.isDark ? 0.18 : 0.10),
        border: Border.all(color: color, width: 1.4),
        boxShadow: [
          BoxShadow(
            color: color.withValues(alpha: palette.isDark ? 0.34 : 0.12),
            blurRadius: 14,
          ),
        ],
      ),
      child: Center(
        child: Text(
          letter,
          style: TextStyle(
            color: color,
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
    );
  }
}

class _RolePill extends StatelessWidget {
  final String role;
  final Color color;
  final _UsersPalette palette;

  const _RolePill({
    required this.role,
    required this.color,
    required this.palette,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      constraints: const BoxConstraints(maxWidth: 118),
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
      decoration: BoxDecoration(
        color: color.withValues(alpha: palette.isDark ? 0.18 : 0.10),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: color.withValues(alpha: 0.62)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(_roleIcon(role), color: color, size: 13),
          const SizedBox(width: 6),
          Flexible(
            child: Text(
              role,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                color: color,
                fontSize: 11,
                fontWeight: FontWeight.w900,
              ),
            ),
          ),
        ],
      ),
    );
  }

  IconData _roleIcon(String role) {
    if (role.contains('MANAGER')) return Icons.verified_user_rounded;
    if (role.contains('WORKER')) return Icons.person_rounded;
    if (role.contains('ADMIN')) return Icons.shield_rounded;
    if (role.contains('SUPERVISOR')) return Icons.groups_rounded;
    return Icons.settings_rounded;
  }
}

class _StatusPill extends StatelessWidget {
  final _UsersPalette palette;

  const _StatusPill({required this.palette});

  @override
  Widget build(BuildContext context) {
    const green = Color(0xFF19D389);
    return Container(
      width: 72,
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 6),
      decoration: BoxDecoration(
        color: green.withValues(alpha: palette.isDark ? 0.17 : 0.10),
        borderRadius: BorderRadius.circular(999),
      ),
      child: const Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.circle, color: green, size: 7),
          SizedBox(width: 5),
          Text(
            'Active',
            style: TextStyle(
              color: green,
              fontSize: 11,
              fontWeight: FontWeight.w900,
            ),
          ),
        ],
      ),
    );
  }
}

class _UserActionButton extends StatelessWidget {
  final IconData icon;
  final Color color;
  final _UsersPalette palette;
  final String tooltip;
  final VoidCallback onPressed;

  const _UserActionButton({
    required this.icon,
    required this.color,
    required this.palette,
    required this.tooltip,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Tooltip(
      message: tooltip,
      child: SizedBox(
        width: 30,
        height: 30,
        child: OutlinedButton(
          onPressed: onPressed,
          style: OutlinedButton.styleFrom(
            foregroundColor: color,
            backgroundColor: palette.actionBackground,
            side: BorderSide(color: color.withValues(alpha: 0.58)),
            padding: EdgeInsets.zero,
            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(6)),
          ),
          child: Icon(icon, size: 15),
        ),
      ),
    );
  }
}

class _IconSquareButton extends StatelessWidget {
  final _UsersPalette palette;
  final IconData icon;
  final VoidCallback? onPressed;

  const _IconSquareButton({
    required this.palette,
    required this.icon,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 42,
      height: 42,
      child: OutlinedButton(
        onPressed: onPressed,
        style: OutlinedButton.styleFrom(
          foregroundColor: palette.textPrimary,
          backgroundColor: palette.control,
          padding: EdgeInsets.zero,
          side: BorderSide(color: palette.tableBorder),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        ),
        child: Icon(icon, size: 18),
      ),
    );
  }
}

class _UsersFooter extends StatelessWidget {
  final _UsersPalette palette;
  final int totalUsers;

  const _UsersFooter({
    required this.palette,
    required this.totalUsers,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final compact = constraints.maxWidth < 760;
        final count = Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.groups_rounded, color: palette.blue, size: 16),
            const SizedBox(width: 8),
            Text(
              'Total Users: $totalUsers',
              style: TextStyle(
                color: palette.textPrimary,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        );
        final pager = Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            _PageButton(icon: Icons.keyboard_double_arrow_left, palette: palette),
            const SizedBox(width: 8),
            _PageButton(icon: Icons.chevron_left_rounded, palette: palette),
            const SizedBox(width: 8),
            Container(
              width: 40,
              height: 34,
              decoration: BoxDecoration(
                color: palette.blue,
                borderRadius: BorderRadius.circular(6),
              ),
              child: const Center(
                child: Text(
                  '1',
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
            ),
            const SizedBox(width: 8),
            _PageButton(icon: Icons.chevron_right_rounded, palette: palette),
            const SizedBox(width: 8),
            _PageButton(
              icon: Icons.keyboard_double_arrow_right,
              palette: palette,
            ),
          ],
        );
        final pageSize = Container(
          height: 34,
          padding: const EdgeInsets.symmetric(horizontal: 14),
          decoration: BoxDecoration(
            color: palette.control,
            borderRadius: BorderRadius.circular(7),
            border: Border.all(color: palette.tableBorder),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                '10 / page',
                style: TextStyle(
                  color: palette.textPrimary,
                  fontWeight: FontWeight.w800,
                ),
              ),
              const SizedBox(width: 12),
              Icon(
                Icons.keyboard_arrow_down_rounded,
                color: palette.textPrimary,
                size: 18,
              ),
            ],
          ),
        );

        if (compact) {
          return Wrap(
            alignment: WrapAlignment.center,
            crossAxisAlignment: WrapCrossAlignment.center,
            spacing: 18,
            runSpacing: 12,
            children: [count, pager, pageSize],
          );
        }

        return Stack(
          alignment: Alignment.center,
          children: [
            Align(alignment: Alignment.centerLeft, child: count),
            pager,
            Align(alignment: Alignment.centerRight, child: pageSize),
          ],
        );
      },
    );
  }
}

class _PageButton extends StatelessWidget {
  final IconData icon;
  final _UsersPalette palette;

  const _PageButton({required this.icon, required this.palette});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 34,
      height: 34,
      child: OutlinedButton(
        onPressed: () {},
        style: OutlinedButton.styleFrom(
          foregroundColor: palette.textPrimary,
          backgroundColor: palette.control,
          padding: EdgeInsets.zero,
          side: BorderSide(color: palette.tableBorder),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(6)),
        ),
        child: Icon(icon, size: 18),
      ),
    );
  }
}

class _UsersPalette {
  final bool isDark;
  final Color panel;
  final Color header;
  final Color row;
  final Color control;
  final Color border;
  final Color tableBorder;
  final Color shadow;
  final Color textPrimary;
  final Color textMuted;
  final Color headerText;
  final Color blue;
  final Color actionBackground;

  const _UsersPalette({
    required this.isDark,
    required this.panel,
    required this.header,
    required this.row,
    required this.control,
    required this.border,
    required this.tableBorder,
    required this.shadow,
    required this.textPrimary,
    required this.textMuted,
    required this.headerText,
    required this.blue,
    required this.actionBackground,
  });

  factory _UsersPalette.of(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    if (isDark) {
      return const _UsersPalette(
        isDark: true,
        panel: Color(0xCC061936),
        header: Color(0xFF0A1A35),
        row: Color(0xB30A1D3A),
        control: Color(0x99071530),
        border: Color(0x8038BDF8),
        tableBorder: Color(0x332E8BFF),
        shadow: Color(0x4D000000),
        textPrimary: Color(0xFFF8FAFC),
        textMuted: Color(0xFF9DB2D8),
        headerText: Color(0xFF91A3C2),
        blue: Color(0xFF168BFF),
        actionBackground: Colors.transparent,
      );
    }

    return const _UsersPalette(
      isDark: false,
      panel: Color(0xF7FFFFFF),
      header: Color(0xFFF8FBFF),
      row: Color(0xF7FFFFFF),
      control: Color(0xFFFFFFFF),
      border: Color(0xFFBFD8FF),
      tableBorder: Color(0xFFD8E6FF),
      shadow: Color(0x140B4AA2),
      textPrimary: Color(0xFF061942),
      textMuted: Color(0xFF365174),
      headerText: Color(0xFF597199),
      blue: Color(0xFF147BFF),
      actionBackground: Color(0xFFFFFFFF),
    );
  }
}

class _ErrorPanel extends StatelessWidget {
  final String message;

  const _ErrorPanel({required this.message});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Text(
          message,
          style: const TextStyle(color: Colors.red),
        ),
      ),
    );
  }
}
