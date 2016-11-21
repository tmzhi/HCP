CREATE TABLE IF NOT EXISTS [Authority] (
  [username] NVARCHAR(20), 
  [authorities] NVARCHAR(20));
  
  CREATE TABLE IF NOT EXISTS [SubInventoryAuthority] (
  [username] NVARCHAR(20), 
  [subinventories] NVARCHAR(200));