local result_list = {}
local i=2

for _,key in pairs(KEYS) do
	local result=redis.call('ZINCRBY',key,ARGV[1],ARGV[i])
    	table.insert(result_list,result)
	i=i+1
end

return result_list
