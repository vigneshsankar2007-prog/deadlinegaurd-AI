/**
 * Student user profile domain model.
 */
class UserModel {
  final int id;
  final String name;
  final String email;
  final String college;
  final String department;
  final int semester;

  const UserModel({
    required this.id,
    required this.name,
    required this.email,
    required this.college,
    required this.department,
    required this.semester,
  });

  UserModel copyWith({
    int? id,
    String? name,
    String? email,
    String? college,
    String? department,
    int? semester,
  }) {
    return UserModel(
      id: id ?? this.id,
      name: name ?? this.name,
      email: email ?? this.email,
      college: college ?? this.college,
      department: department ?? this.department,
      semester: semester ?? this.semester,
    );
  }
}
