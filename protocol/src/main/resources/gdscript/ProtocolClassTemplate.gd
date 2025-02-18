${protocol_note}
class ${protocol_name}:
	${protocol_field_definition}

	func protocolId() -> int:
		return ${protocol_id}

	func get_class_name() -> String:
		return "${protocol_name}"

	func _to_string() -> String:
		const jsonTemplate = "${protocol_json}"
		var params = [${protocol_to_string}]
		return jsonTemplate.format(params, "{}")