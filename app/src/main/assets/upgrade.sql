DROP TABLE IF EXISTS [TransactionInt];
DROP TABLE IF EXISTS [Issue];
DROP TABLE IF EXISTS [Return];
DROP TABLE IF EXISTS [OperationTransaction];
DROP TABLE IF EXISTS [CompletionInfo];

CREATE TABLE IF NOT EXISTS [TransactionInt](
	[ori_wip_entity_name] NVARCHAR(20),
	[component_code] NVARCHAR(20), 
	[component_name] NVARCHAR(50),
	[ori_required_quantity] DECIMAL,
	[remaining_quantity] DECIMAL,
	[wip_entity_name] NVARCHAR(20),
	[required_quantity] DECIMAL,
	[moved_quantity] DECIMAL,
	[create_by] NVARCHAR(20),
	[create_time] DATETIME
);

CREATE TABLE IF NOT EXISTS [Issue](
	[sub_inventory] NVARCHAR(10),
	[wip_entity_name] NVARCHAR(20), 
	[class_code] NVARCHAR(20),
	[start_time] DATETIME,
	[complete_time] DATETIME,
	[component_code] NVARCHAR(20),
	[component_name] NVARCHAR(50),
	[wip_supply_meaning] NVARCHAR(20),
	[issued_quantity] DECIMAL,
	[quantity_open] DECIMAL,
	[remaining_quantity] DECIMAL,
	[transaction_quantity] DECIMAL,
	[create_by] NVARCHAR(20),
	[create_time] DATETIME
);

CREATE TABLE IF NOT EXISTS [ReturnInfo](
	[sub_inventory] NVARCHAR(10),
	[wip_entity_name] NVARCHAR(20), 
	[class_code] NVARCHAR(20),
	[start_time] DATETIME,
	[completion_time] DATETIME,
	[component_code] NVARCHAR(20),
	[component_name] NVARCHAR(50),
	[wip_supply_meaning] NVARCHAR(20),
	[issued_quantity] DECIMAL,
	[quantity_open] DECIMAL,
	[transaction_quantity] DECIMAL,
	[create_by] NVARCHAR(20),
	[create_time] DATETIME
);

CREATE TABLE IF NOT EXISTS [OperationTransaction](
	[wip_entity_name] NVARCHAR(20), 
	[class_code] NVARCHAR(20),
	[start_time] DATETIME,
	[completion_time] DATETIME,
	[transaction_type] NVARCHAR(20),
	[serial_from] NVARCHAR(50),
	[serial_to] NVARCHAR(20),
	[step_from] INT,
	[step_to] INT,
	[transaction_quantity] DECIMAL,
	[create_by] NVARCHAR(20),
	[create_time] DATETIME
);

CREATE TABLE IF NOT EXISTS [CompletionInfo](
	[sub_inventory] NVARCHAR(20), 
	[wip_entity_name] NVARCHAR(20), 
	[class_code] NVARCHAR(20),
	[start_time] DATETIME,
	[completion_time] DATETIME,
	[assembly_code] NVARCHAR(20),
	[assembly_name] NVARCHAR(50),
	[complition_quantity] DECIMAL,
	[remaining_quantity] DECIMAL,
	[remark] NVARCHAR(50),
	[transaction_quantity] DECIMAL,
	[create_by] NVARCHAR(20),
	[create_time] DATETIME
);

CREATE TABLE IF NOT EXISTS [CompletionReturnInfo](
	[sub_inventory] NVARCHAR(20), 
	[wip_entity_name] NVARCHAR(20), 
	[class_code] NVARCHAR(20),
	[start_time] DATETIME,
	[completion_time] DATETIME,
	[assembly_code] NVARCHAR(20),
	[assembly_name] NVARCHAR(50),
	[complition_quantity] DECIMAL,
	[remaining_quantity] DECIMAL,
	[remark] NVARCHAR(50),
	[transaction_quantity] DECIMAL,
	[create_by] NVARCHAR(20),
	[create_time] DATETIME
);

