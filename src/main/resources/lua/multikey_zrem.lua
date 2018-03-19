local result_list = {}
local i=1

for _,key in pairs(KEYS) do
	local result=redis.call('ZREM',key,ARGV[i])
    	table.insert(result_list,result)
	i=i+1
end

return result_list
